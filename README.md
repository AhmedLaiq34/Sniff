# Sniff 👃

Find what fits on Instagram — without endless scrolling

## Quick Summary (TL;DR)

Sniff is an end-to-end mobile application that filters Instagram posts by user-defined keywords (such as shoe sizes). It uses a FastAPI backend, Apify-managed scraping, and an Android client built in Kotlin, deployed via cloud infrastructure. The project demonstrates backend design, third-party API integration, mobile development, and production deployment.

## 🚀 Overview

Sniff helps users quickly find relevant items (e.g., specific shoe sizes) on Instagram pages by filtering posts based on keywords or phrases.

Instead of manually scrolling through dozens of posts, users can scan a page’s recent content and receive only the posts that match their criteria.

## 📥 Download

You can download and install the Android app directly:

### 👉 Download the latest APK

Installation Notes

Download the APK on your Android device

Open the file to install

Enable Install unknown apps when prompted

## 🚀 Features

Scan recent posts from public Instagram accounts

Filter posts by user-defined keywords or phrases (e.g., shoe size)

Server-side filtering for accuracy and performance

Clean Android UI built with Kotlin

Cloud-hosted backend accessible from anywhere

## 🧠 Architecture Overview
Android App (Kotlin)
        |
        v
FastAPI Backend (Python)
        |
        v
Apify Scraping API
        |
        v
Public Instagram Data

## 🛠 Tech Stack
### Backend

FastAPI (Python) – REST API and filtering logic

Apify API – Managed scraping actors for structured Instagram post data

Postman – Backend testing and validation

Render – Cloud deployment and hosting

Frontend

Android Studio

Kotlin + XML

HTTP networking to consume backend APIs

Workflow

GitHub – Source control

Vercel GitHub integration – Automatic backend deployment

### 🔍 How Instagram Data Is Retrieved

The application does not scrape Instagram directly from the mobile app.

The Android app sends a request to the FastAPI backend with:

Instagram username

Number of posts to scan

Keyword or phrase to filter by

The backend calls Apify’s scraping API, which uses managed scraping actors to retrieve structured public post data such as captions, URLs, and timestamps.

The backend applies filtering logic based on user input and returns only relevant results to the app.

This design avoids common scraping issues such as rate limiting and anti-bot protections while keeping the backend focused on processing and serving data.

## 📱 Android App

Built using Kotlin and XML

Consumes the deployed FastAPI backend

Displays filtered results including:

Post caption

Post URL

Posting date

Designed as a lightweight client with all processing handled server-side

## ☁️ Backend Deployment

Backend code is hosted in a GitHub repository

Automatically deployed to Vercel

Exposes a public HTTPS API endpoint

Allows the app to function without local network dependencies

## 🧪 Local Setup Guide
Backend (FastAPI)
Prerequisites

Python 3.10+

An Apify API token

Steps
### Clone the repository
git clone https://github.com/AhmedLaiq34/Sniff.git
cd backend

### Create virtual environment
python -m venv venv
source venv/bin/activate   # Windows: venv\Scripts\activate

### Install dependencies
pip install -r requirements.txt


Create a .env file:

APIFY_API_TOKEN=your_apify_token_here


Run the server:

uvicorn main:app --reload


The API will be available at:

http://127.0.0.1:8000

Android App

Open the project in Android Studio

Update the backend base URL:

Use http://10.0.2.2:8000 for emulator

Use your local IP (e.g. http://192.168.x.x:8000) for physical devices

Build and run the app on an emulator or phone

## 🔮 Future Improvements

Persisting user preferences and search history to deliver more personalized results

Enhanced UI (image previews, richer result cards)

Caching and performance optimizations

Infrastructure-as-Code experimentation using Terraform

Scalable deployments using Azure free-tier services

Advanced CI/CD pipelines with GitHub Actions

## ⚠️ Disclaimer

This project processes publicly available Instagram data only, retrieved via a third-party scraping service.
No private or authenticated content is accessed.

## 📌 Project Status

This project is currently an MVP and under active refinement.
