import re
import pdfplumber
import json


EDITION_TO_YEAR = {
    "RRIOC-6": 2013,
    "RRIOC-7": 2014,
    "RRIOC-8": 2015,
}

def get_year_of_publication(url):
    match = re.search(r"articole/([^/]+)", url)
    if not match:
        return None

    filename = match.group(1) 

    #the year is specified in the URL (2008-2012)
    year_match = re.search(r"(\d{4})", filename)
    if year_match:
        return int(year_match.group(1))

    #the year is not specified in the URL (2013-2015)
    for edition, year in EDITION_TO_YEAR.items():
        if filename.startswith(edition):
            return year

    return None

def pdf_to_jsonl(pdf_path, jsonl_path, source_article, source_url, min_words=240, max_words=270):
    
    year_of_publication = get_year_of_publication(source_url)
    if year_of_publication is None:
        print(f"Unknown year: {source_url}")

    full_text = ""
    with pdfplumber.open(pdf_path) as pdf:
        for page in pdf.pages:
            text = page.extract_text()
            if text:
                text = text.replace("ţ", "ț").replace("ş", "ș").replace("Ţ", "Ț").replace("Ş", "Ș")

                #cids - pdfplumber's unknown characters
                text = re.sub(r"cid:\w+", "", text) 
                full_text += text + " "

    #extract the text from the PDF and split at every whitespace
    words = full_text.split() 
    chunks = []

    start_idx = 0
    n = len(words)

    while start_idx < n:
        end_idx = min(start_idx + max_words, n)

        #if the end of the entire text has been reached, add everything that remains
        #to the JSON, even if it does not meet the [min,max] word count.
        if end_idx == n:
            chunk_words = words[start_idx:end_idx]
            chunks.append(" ".join(chunk_words))
            break

        #find the first . after the minimum required number of words
        #for writing the chunk instance to the dataset.
        search_start = start_idx + min_words

        for i in range(search_start, end_idx):
            if words[i].endswith("."):
                end_idx = i + 1 
                break

        #if no . is found between [min,max], use the maximum number of words 
        #defined for the current chunk when adding it to the dataset.
        chunk_words = words[start_idx:end_idx]
        chunks.append(" ".join(chunk_words))
        start_idx = end_idx

    #remove first and last chunk (generally they have the introduction and conclusion)
    if len(chunks) > 2:
        chunks = chunks[1:-1]  #keep middle chunks
                
        with open(jsonl_path, "a", encoding="utf-8") as f:
            for i, chunk in enumerate(chunks, start=1):
                entry = {
                    "text": chunk,
                    "label": "human",
                    "AI_used": "none",
                    "chunk_no": i,
                    "source": source_article,
                    "source_url": source_url,
                    "year_of_publication": year_of_publication,
                }
                f.write(json.dumps(entry, ensure_ascii=False) + "\n")
                
    elif len(chunks) <= 2:
        print("PDF has 1 or 2 chunks. Nothing is kept.")
        chunks = []

    print(f"Nr of words: {len(words)}")
    print(f"Nr of chunks: {len(chunks) + 2 if len(chunks) > 0 else 'not enough chunks'}")
    print(f"Nr of saved chunks (without first and last): {len(chunks)}")