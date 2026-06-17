# Fix for 413 Request Entity Too Large Error

## 1. Update /etc/nginx/nginx.conf

Add this line inside the `http` block (after line 12):

```nginx
client_max_body_size 100M;
```

## 2. Update /etc/nginx/sites-available/nguonevents.com

Add `client_max_body_size 100M;` to the HTTPS server block (section 3), right after the `server_name` line:

```nginx
server {
    listen 443 ssl;
    server_name nguonevents.com www.nguonevents.com;
    
    client_max_body_size 100M;  # Add this line
    
    ssl_certificate /etc/letsencrypt/live/nguonevents.com/fullchain.pem;
    # ... rest of config
}
```

## 3. Apply Changes

Run these commands:

```bash
# Test nginx configuration
sudo nginx -t

# Reload nginx
sudo systemctl reload nginx
```

## Notes:
- `100M` allows uploads up to 100MB. Adjust as needed (e.g., `50M`, `200M`)
- Adding it to nginx.conf applies globally
- Adding it to the server block applies only to that domain
- Both locations ensure the setting is active
