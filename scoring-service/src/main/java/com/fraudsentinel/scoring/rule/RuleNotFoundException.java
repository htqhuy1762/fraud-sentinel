package com.fraudsentinel.scoring.rule;

public class RuleNotFoundException extends RuntimeException {
    public RuleNotFoundException(String code) { super("Rule " + code + " not found"); }
}