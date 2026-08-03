from pathlib import Path

input_folder = Path("train")
output_file = "train.jsonl"

#input_folder = Path("test")
#output_file = "test.jsonl"

input_files = sorted(input_folder.glob("*.jsonl"))

with open(output_file, "w", encoding="utf-8") as outfile:
    for file_path in input_files:
        with open(file_path, "r", encoding="utf-8") as infile:
            for line in infile:
                outfile.write(line)

