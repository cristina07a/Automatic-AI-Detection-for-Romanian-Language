import json
import random
from pathlib import Path


DIR = Path(__file__).parent

SEED = 42
random.seed(SEED)

records = []
with open(DIR / "merged_dataset.jsonl", encoding="utf-8") as f:
    for line in f:
        records.append(json.loads(line))
        
human   = [r for r in records if r["AI_used"] == "none"]
gpt     = [r for r in records if r["AI_used"] == "gpt-4-turbo"]
claude  = [r for r in records if r["AI_used"] == "claude-sonnet-4-5"]
gemini  = [r for r in records if r["AI_used"] == "gemini-3.5-flash-lite"]


n = len(human) // 4
H = [human[i*n:(i+1)*n] for i in range(4)]
H[3] += human[4*n:]


H_test_fixed = H[3]         
H_train_pool = H[0] + H[1] + H[2]  

print(f"H_train_pool: {len(H_train_pool)} | H_test_fixed: {len(H_test_fixed)}")

folds = [
    {"name": "fold1_gpt",    "train_ai": claude + gemini, "test_ai": gpt},
    {"name": "fold2_claude", "train_ai": gpt + gemini,    "test_ai": claude},
    {"name": "fold3_gemini", "train_ai": gpt + claude,    "test_ai": gemini},
]

def write_jsonl(path, records):
    with open(path, "w") as f:
        for r in records:
            f.write(json.dumps(r) + "\n")

for fold in folds:
    fold_dir = DIR / fold["name"]
    fold_dir.mkdir(exist_ok=True)

    train = H_train_pool + fold["train_ai"]
    test  = H_test_fixed + fold["test_ai"]

    random.shuffle(train)
    random.shuffle(test)

    write_jsonl(fold_dir / "train.jsonl", train)
    write_jsonl(fold_dir / "test.jsonl",  test)

    train_labels = [r["label"] for r in train]
    test_labels  = [r["label"] for r in test]
    test_ai_used = [r["AI_used"] for r in test if r["label"] == "AI"]

    print(f"\n── {fold['name']} ──")
    print(f"  Train: {len(train)} (human={train_labels.count('human')}, AI={train_labels.count('AI')})")
    print(f"  Test:  {len(test)}  (human={test_labels.count('human')}, AI={test_labels.count('AI')})")
    print(f"  Test AI source: {set(test_ai_used)}")
