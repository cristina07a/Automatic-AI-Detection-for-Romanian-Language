import os
import requests
from extracting_text import pdf_to_jsonl

def download_pdf(url, save_folder="downloaded_pdfs"):
    os.makedirs(save_folder, exist_ok=True)
    filename = url.split("/")[-1]
    save_path = os.path.join(save_folder, filename)

    response = requests.get(url)
    if response.status_code == 200:
        with open(save_path, "wb") as f:
            f.write(response.content)
        print(f"Saved: {save_path}")
        return save_path
    else:
        print(f"Link {url} could not be downloaded")
        return None

def process_urls_file(urls_file, jsonl_path="dataset_human.jsonl"):
    with open(urls_file, "r", encoding="utf-8") as f:
        urls = [line.strip() for line in f if line.strip()]

    for source_article, url in enumerate(urls, start=1):
        pdf_path = download_pdf(url)

        if pdf_path:
            pdf_to_jsonl(pdf_path, jsonl_path, source_article=source_article, source_url = url, min_words=220, max_words=250)

        #delete the downloaded PDF
        try:
            os.remove(pdf_path)
            print(f"Deleted: {pdf_path}")
        except Exception as e:
            print(f"Could not delete: {pdf_path}")

if __name__ == "__main__":
    process_urls_file("urls.txt")