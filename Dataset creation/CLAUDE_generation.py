import itertools
import anthropic
import json
import random

client = anthropic.Anthropic(api_key=" ")

prompts = [
    "Parafrazează textul de mai jos în 250-270 cuvinte păstrând sensul original.",
    "Rescrie textul într-un mod diferit în 250-270 cuvinte, dar menține ideea principală",
    "Păstrează ideea de bază, exprim-o în alte cuvinte în 250-270 cuvinte",
]

input_file = "dataset_human.jsonl"
output_file = "dataset_CLAUDE.jsonl"

start_index = 0
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
    
    response = client.messages.create(
        model="claude-sonnet-4-5",
        max_tokens=1000,
        temperature = 0.7,
        messages=[
            {"role": "user", "content": full_prompt}
        ]
        )
        
    output_text = response.content[0].text
        
    result = {
        "text": output_text.strip(),
        "label": "AI",
        "AI_used": "claude-sonnet-4-5",
        "chunk_no": ex["chunk_no"],
        "source": ex["source"],
        "source_url": ex["source_url"],
        "year_of_publication": ex["year_of_publication"],
    }
        
    with open(output_file, 'a', encoding='utf-8') as out_f:
        out_f.write(json.dumps(result, ensure_ascii=False) + "\n")

    print(f"\n Index {start_index + i + 1} was paraphrased successfully. ")
    print(f"> Output: {output_text[:20]}...\n")