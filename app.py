# backend/app.py
from fastapi import FastAPI, HTTPException
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
    
@app.get("/ping")
def ping():
    return {"message": "pong from FastAPI!"}


if __name__ == "__main__":
    uvicorn.run("app:app", host="0.0.0.0", port=8000, reload=True)
