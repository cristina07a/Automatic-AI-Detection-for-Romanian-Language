import json
import re
import sys
from collections import defaultdict


def word_tokenize(text):
    #extracts the words from label
    return re.findall(r"\b\w+\b", text, flags=re.UNICODE)


def analyze(path):
    groups = defaultdict(lambda: {"texts": 0, "total_words": 0, "total_wordlen_sum": 0.0})

    with open(path, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            d = json.loads(line)
            text = d.get("text", "") or ""
            label = d.get("label", "unknown")
            ai = d.get("AI_used", "unknown")

            words = word_tokenize(text)
            n_words = len(words)
            avg_word_len_this_text = (
                sum(len(w) for w in words) / n_words if n_words > 0 else 0
            )

            g = groups[(label, ai)]
            g["texts"] += 1
            g["total_words"] += n_words
            g["total_wordlen_sum"] += avg_word_len_this_text

    return groups


def main():
    path = sys.argv[1] if len(sys.argv) > 1 else "merged_dataset.jsonl"
    groups = analyze(path)

    print("Analysis based on AI_used")
    for (label, ai) in sorted(groups.keys()):
        g = groups[(label, ai)]
        avg_seq_len = g["total_words"] / g["texts"]
        avg_word_len = g["total_wordlen_sum"] / g["texts"]
        print(f"AI_used: {ai}; avg_seq_len={avg_seq_len:.2f} words, avg_word_len={avg_word_len:.3f} characters")

    label_groups = defaultdict(lambda: {"texts": 0, "total_words": 0, "total_wordlen_sum": 0.0})
    for (label, ai), g in groups.items():
        lg = label_groups[label]
        lg["texts"] += g["texts"]
        lg["total_words"] += g["total_words"]
        lg["total_wordlen_sum"] += g["total_wordlen_sum"]

    print("\nHuman/AI analysis")
    for label, lg in sorted(label_groups.items()):
        avg_seq_len = lg["total_words"] / lg["texts"]
        avg_word_len = lg["total_wordlen_sum"] / lg["texts"]
        print(
            f"{label}; avg_seq_len={avg_seq_len:.2f} words, avg_word_len={avg_word_len:.3f} characters"
        )

if __name__ == "__main__":
    main()