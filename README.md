# Overview
This project contains the construction of a new 
[dataset](https://www.kaggle.com/datasets/tomoiucristina/english-ai-generated-text-detection-dataset) for Romanian AI-generated text detection available on Kaggle 
and the corresponding evaluation experiments. The trained models are available on [HuggingFace](https://huggingface.co/collections/cristina07a/romanian-ai-text-detection-models). 
The dataset is available on [Kaggle](https://www.kaggle.com/datasets/tomoiucristina/romanian-ai-generated-text-detection-dataset).

Evaluation details:
- 80/20 train/test split and, if not possible, a ratio as close as possible
- random_state = 42
- all models were trained using NVIDIA A100 GPU provided by Google Colab
- all LLMs were trained for 3 epochs and were optimized using Adam with a learning rate of 2e-5
- best standard models were selected using early stopping/best depth depending on the model type.

  The best performing model was integrated into an Android Application.
  
## Contents

<img width="885" height="163" alt="image" src="https://github.com/user-attachments/assets/118981a4-016b-431c-a422-ec6247c68587" />

The dataset has 7 attributes: text, label, AI_used, chunk_no, source, source_url, year_of_publication.
- text: plain text chunked/generated.
- label: human or AI
- AI_used: model used for text generation (none, claude-sonnet-4-5, gemini-3.5-flash-lite, gpt-4-turbo)
- chunk_no: the sequential index of the chunk within its source document (used to trace chunks back to the original text and to perform the document-level split).
- source: index of the article the text was collected from.
- source_url: the URL of the original article.
- year_of_publication: the publication year of the original source text.

It contains 6120 instances of human-written and AI-generated texts in English, evenly split between the two classes (3060 each). 
Human-written texts were sourced from articles published in RRIOC (2008-2015). AI-generated texts were produced by 
multiple AIs using paraphrasing prompts.

## Evaluation
Types of evaluation:
- Standard evaluation: 80/20 train/test split with random_state = 42
- Leave-one-generator-out evaluation: each fold excludes one AI generator (Claude, GPT, Gemini) from training and uses it exclusively for testing
- Document level split: train/test split performed at the document level rather than the chunk level, to prevent chunks from the same document appearing in both train and test

BERT is the best model overall. It does not always have the highest accuracy, but is the most consistent across
all three evaluations, especially at generalizing to unseen generators. It's also fastest to train and ties for 
top accuracy in the classic evaluation.

## Android Application
The application has the following functionalities:
- User authentication (registration, login, logout): Users can create an account
using an email address and password . After registration, login grants access to
the main functionalities of the application, such as content verification and viewing
previous analyses. The logout option redirects the user to the login screen.
- Language settings: The user can switch the interface language between Romanian
and English in the settings menu.
- Content verification: Users can upload plain text, PDF documents, or images for
verification. For images, the text is extracted automatically using Optical Character
Recognition (OCR).
- Language selection for classification: Before running the prediction, the user
must specify the language (Romanian or English). The selected language determines
which model will be used for classification.
- Text preprocessing: Uploaded text is tokenised and divided into smaller chunks
to ensure compatibility with the classification model’s input size.
- Prediction and report generation: After processing, the system generates a
PDF report that includes:
–- An overview: the number of chunks analysed, how many were classified as AI-
generated or human-written, and the average AI percentage across all fragments.
–- A detailed section: each text fragment is displayed with a colour indicator (red
for AI-generated, green for human-written) and its AI confidence score.
- History management: Users have access to a complete history of their previous
verifications. Each record includes part of the analysed text and a button to open
the associated PDF report.

## Additional screenshots
<p align="center">
<img width="260" height="444" alt="image" src="https://github.com/user-attachments/assets/bbce5e40-0e8d-4e3a-b4a0-f492a1b6ff82" />
</p>

Each report follows a predefined template, structured into an introduction, an overview, and then a per-section analysis.

The introduction includes the app's logo, the text "ARTIFICIAL INTELLIGENCE DETECTION", and the title "CONTENT ANALYSIS". 
The purpose of this section is to give the document a professional appearance.

The overview presents a high-level summary of all analyzed fragments: the number of batches resulting from splitting the tokenized text 
("Total parts analyzed"), the fragments classified as over 50% AI ("AI detected parts"), those under 50% AI ("Not AI parts"), 
and the average AI confidence percentage across all batches ("Average AI confidence").

<p align="center">
<img width="222" height="444" alt="Screenshot_20260804_104548" src="https://github.com/user-attachments/assets/ef1ae86f-b8fb-4388-a5c3-94fb435aa466" />
<img width="222" height="444" alt="Screenshot_20260804_104546" src="https://github.com/user-attachments/assets/ddf3795c-62c0-4dcd-8be2-dc127d271e2e" />
<img width="222" height="444" alt="Screenshot_20260804_104541" src="https://github.com/user-attachments/assets/16ff178b-9d11-4bc3-984a-58f849cba281" />
<img width="222" height="444" alt="Screenshot_20260804_104527" src="https://github.com/user-attachments/assets/1664bb77-d62a-4b96-a6d9-e4df1e161fce" />
</p>
