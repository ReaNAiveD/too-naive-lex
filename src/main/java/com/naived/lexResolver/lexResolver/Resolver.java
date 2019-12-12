package com.naived.lexResolver.lexResolver;

import com.naived.lexResolver.exception.LexFileParseException;
import com.naived.lexResolver.exception.ReParseException;

import java.io.InputStream;

public interface Resolver {
    LexSource resolve(InputStream source) throws LexFileParseException, ReParseException;
}
