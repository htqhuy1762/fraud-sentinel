package com.fraudsentinel.scoring.rule;

import com.fraudsentinel.scoring.common.NotFoundException;

public class RuleNotFoundException extends NotFoundException {
    public RuleNotFoundException(String code) { super("Rule " + code + " not found"); }
}