# 🚀 Smart IT Helpdesk Ticketing System - Deployment Guide

This guide walks you through deploying the complete full-stack application. By the end of this guide, your system will be running on real cloud servers interconnected properly.

## 🏗️ Deployment Architecture Flow
```mermaid
graph LR
A[React Frontend <br/> (Vercel)] -->|REST API Calls <br/> over HTTPS| B(Spring Boot Backend <br/> Docker on Render)
B -->|JDBC Connection| C[(MySQL Database <br/> Aiven / AWS)]
```

---

## Step 1: Deploying the MySQL Cloud Database
For a free, reliable cloud database, we recommend **Aiven** or **Render's managed MySQL**.

1. Create a free account on [Aiven.io](https://aiven.io/) or your preferred cloud DB provider.
2. Spin up a new MySQL service.
3. Obtain your connection credentials. You will need:
   - **Host** (e.g., `mysql-xxyyzz.aivencloud.com`)
   - **Port** (e.g., `27101`)
   - **Username** (e.g., `avnadmin`)
   - **Password**
4. Keep these safe for Step 2.

---

## Step 2: Deploying the Spring Boot Backend (Render)

Render makes deploying Java applications via our included `Dockerfile` extremely easy.

1. Go to [Render.com](https://render.com/), sign up, and create a new **Web Service**.
2. Connect your GitHub repository containing the `helpdesk-backend` code.
3. Select **Docker** as your runtime environment. Let Render look for the `Dockerfile`.
4. Very Important: **Scroll down to 'Environment Variables'** and inject your Database credentials exactly like this:
   - `DATABASE_URL` = `jdbc:mysql://[YOUR_DB_HOST]:[YOUR_DB_PORT]/defaultdb?createDatabaseIfNotExist=true`
   - `DATABASE_USERNAME` = `[YOUR_DB_USERNAME]`
   - `DATABASE_PASSWORD` = `[YOUR_DB_PASSWORD]`
   - `JWT_SECRET` = `GenerateARandomVeryLongStringHereForSecurity`
   - `FRONTEND_URL` = `*` *(We will change this to the Vercel URL later)*
5. Click **Deploy**.
6. Once deployed, Render will give you an API URL (e.g., `https://helpdesk-backend.onrender.com`). Copy this!

---

## Step 3: Deploying the React Frontend (Vercel)

Now that your backend is alive, we deploy the frontend to Vercel.

1. First, inside your local `helpdesk-hero-main` code folder, locate `.env.production`.
2. Update the variable inside it with your new Render URL:
   ```env
   VITE_API_URL=https://helpdesk-backend.onrender.com
   ```
3. Push your code to GitHub.
4. Go to [Vercel.com](https://vercel.com) and create a new Project.
5. Select your frontend repository.
6. The framework preset should automatically detect `Vite`.
7. Expand "Environment Variables" and add:
   - Name: `VITE_API_URL`
   - Value: `https://helpdesk-backend.onrender.com`
8. Click **Deploy**.

---

## Step 4: Security Polish (Optional but Recommended)

Once Vercel gives you your frontend URL (e.g., `https://my-helpdesk.vercel.app`):
1. Go back to your Backend on Render.
2. Edit your environment variables.
3. Change `FRONTEND_URL` from `*` to `https://my-helpdesk.vercel.app`.
4. This strictly enforces **CORS**, meaning ONLY your Vercel website is technically allowed to communicate with your backend database, massively boosting security against hackers.

🎉 **Done!** Your Full-Stack Helpdesk is completely deployed.
