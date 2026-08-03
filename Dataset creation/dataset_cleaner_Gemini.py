import json
import re

INPUT_PATH = "dataset_GEMINI.jsonl"         
OUTPUT_PATH = "dataset_GEMINI_cleaned.jsonl" 
FIELD_NAME = "text" #the field that will be cleaned

# Iată ... \n\n
pattern = re.compile(r'^Iată[^\n]*\n\n')

cleaned_count = 0
untouched_count = 0

with open(INPUT_PATH, "r", encoding="utf-8") as f_in, \
     open(OUTPUT_PATH, "w", encoding="utf-8") as f_out:

    for line in f_in:
        line = line.strip()
        if not line:
            continue

        obj = json.loads(line)
        text = obj.get(FIELD_NAME, "")

        match = pattern.match(text)
        if match:
            obj[FIELD_NAME] = text[match.end():]
            cleaned_count += 1
        else:
            untouched_count += 1

        f_out.write(json.dumps(obj, ensure_ascii=False) + "\n")

print(f"Nr of cleaned instances: {cleaned_count}")
print(f"Nr of unchanged instances: {untouched_count}")