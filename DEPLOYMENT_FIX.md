# Fix for 404 File Access from External Devices

## Problem
Files uploaded from external devices can't be accessed from other machines because:
1. MinIO uses internal Docker network URL
2. External URL points to HTTP port 9000 (not accessible externally)
3. Frontend calls wrong endpoint format

## Solution

### Step 1: Update Nginx Configuration

Edit `/etc/nginx/sites-available/nguonevents.com` and replace the HTTPS server block (section 3) with:

```bash
sudo nano /etc/nginx/sites-available/nguonevents.com
```

Replace section 3 with the content from `nginx-updated-config.txt`

### Step 2: Test and Reload Nginx

```bash
sudo nginx -t
sudo systemctl reload nginx
```

### Step 3: Rebuild and Restart Backend

```bash
cd ~/nguon-app
docker-compose down backend
docker-compose up -d --build backend
```

### Step 4: Update Frontend API Calls

Your frontend should call files using one of these formats:

**Option 1** (recommended): Use the full filename returned from upload
```javascript
// After upload, you get: fileName = "media/1234567890_photo.jpg"
const imageUrl = `https://nguonevents.com/api/files/view?path=${fileName}`;
```

**Option 2**: Use folder + filename
```javascript
const imageUrl = `https://nguonevents.com/api/files/view/media/photo.jpg`;
```

**Option 3**: Use presigned URL (already working)
```javascript
// Use the presignedUrl returned from upload response
const imageUrl = response.presignedUrl;
```

## What Changed

1. **Backend**: Added new endpoint `/api/files/view?path=...` to accept full file path
2. **Nginx**: Added `/minio/` proxy to make MinIO accessible externally via HTTPS
3. **Config**: Changed MinIO external URL from `http://167.86.120.214:9000` to `https://nguonevents.com/minio`

## Testing

After deployment, test with:
```bash
# Upload a file and note the fileName in response
curl -X POST https://nguonevents.com/api/files/upload/media -F "file=@test.jpg"

# View the file using the returned fileName
curl https://nguonevents.com/api/files/view?path=media/1234567890_test.jpg
```
