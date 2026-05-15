# backend/app.py
from fastapi import FastAPI, HTTPException
from openai import OpenAI
from pydantic import BaseModel
from apify_client import ApifyClient
from typing import Optional, List
from dotenv import load_dotenv
import os
import uvicorn

# Load environment variables
load_dotenv()

APIFY_TOKEN = os.getenv("APIFY_TOKEN")
if not APIFY_TOKEN:
    raise ValueError("Missing APIFY_TOKEN. Add it to your .env file.")

client = ApifyClient(APIFY_TOKEN)

# Groq LLM client (free tier - Llama 3.3 70B)
GROQ_API_KEY = os.getenv("GROQ_API_KEY")
groq_client = OpenAI(
    api_key=GROQ_API_KEY,
    base_url="https://api.groq.com/openai/v1"
)
app = FastAPI(title="Instagram Phrase Finder API")

# ----- Existing Models -----
class ScrapeRequest(BaseModel):
    username: str
    max_posts: Optional[int] = 50
    newer_than: Optional[str] = None

class Post(BaseModel):
    url: str
    caption: Optional[str] = None
    timestamp: Optional[str] = None

class ScrapeResponse(BaseModel):
    status: str
    posts: List[Post] = []

# ----- New Models for Search -----
class SearchRequest(BaseModel):
    username: str
    phrase: str
    max_posts: Optional[int] = 5

class Match(BaseModel):
    post_url: str
    caption_snippet: Optional[str] = None
    date: Optional[str] = None
    owner_comment: Optional[str] = None

class SearchResponse(BaseModel):
    status: str
    matches: List[Match] = []

# ----- Original Endpoint -----
@app.post("/api/scrape_instagram", response_model=ScrapeResponse)
def scrape_instagram(req: ScrapeRequest):
    username = req.username.strip()
    if not username:
        raise HTTPException(status_code=400, detail="username is required")

    try:
        run_input = {
            "username": [username],
            "resultsLimit": req.max_posts,
            "resultsType": "posts",
            "addParentData": True
        }

        if req.newer_than:
            run_input["scrapePostsUntilDate"] = req.newer_than

        run = client.actor("apify/instagram-post-scraper").call(run_input=run_input)
        dataset_items = list(client.dataset(run["defaultDatasetId"]).list_items().items)

        posts = []
        for item in dataset_items:
            posts.append(
                Post(
                    url=item.get("url", ""),
                    caption=item.get("caption", ""),
                    timestamp=item.get("timestamp"),
                )
            )

        return ScrapeResponse(status="success", posts=posts)

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Apify error: {str(e)}")

# ----- NEW Endpoint for Phrase Search -----
@app.post("/api/search_instagram", response_model=SearchResponse)
def search_instagram(req: SearchRequest):
    username = req.username.strip()
    phrase = req.phrase.strip().lower()

    if not username or not phrase:
        raise HTTPException(status_code=400, detail="username and phrase are required")

    try:
        run_input = {
            "username": [username],
            "resultsLimit": req.max_posts,
            "resultsType": "posts",
            "addParentData": True
        }

        run = client.actor("apify/instagram-post-scraper").call(run_input=run_input)
        dataset_items = list(client.dataset(run["defaultDatasetId"]).list_items().items)

        matches = []
        print(f"🔍 Scanning {len(dataset_items)} posts for phrase '{phrase}'")

        for item in dataset_items:
            caption = (item.get("caption") or "").lower()
            owner_comment = (item.get("ownerComments") or "").lower()

            # Debug print all captions to confirm data from Apify
            print(f"\n📸 POST: {item.get('url', '')}")
            print(f"📝 Caption: {caption[:80]}...")
            print(f"💬 Owner comment: {owner_comment[:80]}...")

            if phrase in caption or phrase in owner_comment:
                matches.append(
                    Match(
                        post_url=item.get("url", ""),
                        caption_snippet=item.get("caption", "")[:200],
                        date=item.get("timestamp"),
                        owner_comment=item.get("ownerComments", ""),
                    )
                )

        print(f"✅ Found {len(matches)} matches.")
        return SearchResponse(status="success", matches=matches)

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Apify error: {str(e)}")

# ----- LLM Summarization Models -----
class PostSummary(BaseModel):
    caption: Optional[str] = None
    url: str
    timestamp: Optional[str] = None

class SummarizeRequest(BaseModel):
    username: str
    keyword: str
    posts: List[PostSummary]

class SummarizeResponse(BaseModel):
    status: str
    summary: str

# ----- LLM Summarization Endpoint -----
@app.post("/api/summarize", response_model=SummarizeResponse)
def summarize_results(req: SummarizeRequest):
    username = req.username.strip()
    keyword = req.keyword.strip()

    if not username or not keyword or not req.posts:
        raise HTTPException(status_code=400, detail="username, keyword, and posts are required")

    try:
        # Build compact post list for prompt (limit to first 10 to save tokens)
        posts_for_prompt = req.posts[:10]
        posts_text = "\n".join([
            f"- Post {i+1}: \"{(p.caption or 'No caption')[:150]}\" ({p.timestamp or 'unknown date'}) → {p.url}"
            for i, p in enumerate(posts_for_prompt)
        ])

        prompt = f"""You are Sniff AI, an Instagram post analyst.
A user searched for "{keyword}" in @{username}'s posts and found {len(req.posts)} matching posts.

Here are the matches (up to 10):
{posts_text}

Write a concise 2-3 sentence insight. Mention key themes, time patterns, and a useful takeaway. Stay under 60 words."""

        response = groq_client.chat.completions.create(
            model="llama-3.3-70b-versatile",
            messages=[
                {"role": "system", "content": "You are a concise Instagram post analyst. Respond in 2-3 sentences only."},
                {"role": "user", "content": prompt}
            ],
            max_tokens=200,
            temperature=0.7,
        )

        summary = response.choices[0].message.content.strip()
        return SummarizeResponse(status="success", summary=summary)

    except Exception as e:
        print(f"❌ Groq API error: {str(e)}")
        # Graceful fallback — return a basic summary instead of failing
        fallback = f"Found {len(req.posts)} posts mentioning \"{keyword}\" from @{username}."
        return SummarizeResponse(status="fallback", summary=fallback)
    
@app.get("/ping")
def ping():
    return {"message": "pong from FastAPI!"}


if __name__ == "__main__":
    uvicorn.run("app:app", host="0.0.0.0", port=8000, reload=True)
