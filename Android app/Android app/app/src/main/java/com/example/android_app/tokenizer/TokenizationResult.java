package com.example.android_app.tokenizer;

import java.util.List;

public class TokenizationResult {
    public List<String> tokens;
    public List<UnknownToken> unknownTokens;

    public TokenizationResult(List<String> tokens, List<UnknownToken> unknownTokens) {
        this.tokens = tokens;
        this.unknownTokens = unknownTokens;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public List<UnknownToken> getUnknownTokens() {
        return unknownTokens;
    }
}