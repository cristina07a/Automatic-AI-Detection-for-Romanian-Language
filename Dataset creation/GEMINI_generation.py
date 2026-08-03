import google.generativeai as genai
import json
import random
import itertools

API_KEY = " " 
genai.configure(api_key=API_KEY)


model = genai.GenerativeModel('gemini-3.5-flash-lite')


prompts = [
    "Parafrazează textul de mai jos în 250-270 cuvinte păstrând sensul original.",
    "Rescrie textul într-un mod diferit în 250-270 cuvinte, dar menține ideea principală"
    "Păstrează ideea de bază, exprim-o în alte cuvinte în 250-270 cuvinte",    
]

input_file = "dataset_human.jsonl"
output_file = "dataset_GEMINI.jsonl"
start_index = 2040
num_examples = 1020

examples = []
with open(input_file, 'r', encoding='utf-8') as f:
    selected_lines = itertools.islice(f, start_index, start_index + num_examples)
    for line in selected_lines:
        examples.append(json.loads(line))
        
for i, ex in enumerate(examples):
    text = ex['text']
    prompt = random.choice(prompts)
    full_prompt = f"{prompt}\n\n{text}"
    response = model.generate_content(full_prompt)

    output_text = response.text

    result = {
        "text": output_text,
        "label": "AI",
        "AI_used": "gemini-3.5-flash-lite",
        "chunk_no": ex["chunk_no"],
        "source": ex["source"],
        "source_url": ex["source_url"],
        "year_of_publication": ex["year_of_publication"],
    }

    with open(output_file, 'a', encoding='utf-8') as out_f:
        out_f.write(json.dumps(result, ensure_ascii=False) + "\n")

    print(f"\n Indicele {start_index + i + 1} a fost interpretat de AI ")
    print(f"> Output: {output_text[:20]}...\n")
