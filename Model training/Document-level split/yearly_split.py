import json
import os
import pandas as pd
from sklearn.model_selection import train_test_split

INPUT_PATH = "merged_dataset.jsonl"
OUTPUT_DIR = "." 
TEST_FRACTION = 0.2 
RANDOM_STATE = 42

data = []
with open(INPUT_PATH, "r", encoding="utf-8") as f_in:
    for line in f_in:
        line = line.strip()
        if not line:
            continue
        data.append(json.loads(line))

df = pd.DataFrame(data)
print(f"Total instances (chunks) uploaded: {len(df)}")

documents = df[['year_of_publication', 'source']].drop_duplicates().reset_index(drop=True)
print(f"Total number of articles: {len(documents)}")
print(documents['year_of_publication'].value_counts().sort_index())

train_docs_list = []
test_docs_list = []

for year, group in documents.groupby('year_of_publication'):

    train_g, test_g = train_test_split(
        group,
        test_size=TEST_FRACTION,
        random_state=RANDOM_STATE
    )
    train_docs_list.append(train_g)
    test_docs_list.append(test_g)
    print(f"Year {year}: {len(train_g)} train / {len(test_g)} test (out of {len(group)} documents)")

train_docs = pd.concat(train_docs_list, ignore_index=True)
test_docs = pd.concat(test_docs_list, ignore_index=True) if test_docs_list else pd.DataFrame(columns=documents.columns)

print(f"\nNumber of documents -> train: {len(train_docs)}, test: {len(test_docs)}")

train_keys = set(zip(train_docs['year_of_publication'], train_docs['source']))
test_keys = set(zip(test_docs['year_of_publication'], test_docs['source']))
overlap = train_keys & test_keys
print(f"Overlap documents train/test: {len(overlap)}")
assert len(overlap) == 0, "Overlapping documents for training and testing"

def save_documents(doc_list_df, split_name):
    split_dir = f"{OUTPUT_DIR}/{split_name}"
    os.makedirs(split_dir, exist_ok=True)

    total_chunks = 0
    for _, doc_row in doc_list_df.iterrows():
        year = doc_row['year_of_publication']
        source = doc_row['source']

        doc_data = df[
            (df['year_of_publication'] == year) & (df['source'] == source)
        ].sort_values('chunk_no')

        output_path = f"{split_dir}/{year}_{source}.jsonl"
        with open(output_path, "w", encoding="utf-8") as f_out:
            for _, row in doc_data.iterrows():
                f_out.write(json.dumps(row.to_dict(), ensure_ascii=False) + "\n")

        total_chunks += len(doc_data)

    print(f"{split_name}: {len(doc_list_df)} documents, {total_chunks} chunks saved in '{split_dir}/'")

save_documents(train_docs, "train")
save_documents(test_docs, "test")