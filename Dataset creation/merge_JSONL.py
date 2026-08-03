input_files = [
    "dataset_human.jsonl",
    "dataset_CLAUDE_cleaned.jsonl",
    "dataset_GEMINI_cleaned.jsonl",
    "dataset_GPT.jsonl",
]

output_file = "merged_dataset.jsonl"

with open(output_file, 'w', encoding='utf-8') as outfile:
    for filename in input_files:
        with open(filename, 'r', encoding='utf-8') as infile:
            for line in infile:
                outfile.write(line)
