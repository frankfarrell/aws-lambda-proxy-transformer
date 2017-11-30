package com.github.frankfarrell.proxytransformer.parser;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by ffarrell on 24/11/2017.
 */
public class ExpressionParserTest {

    ExpressionParser expressionParserUnderTest;

    @Before
    public void setup(){
        expressionParserUnderTest =
                new ExpressionParser(Collections.singletonMap("statusCode", () -> "help"),
                        Collections.singletonMap("toUpper", x -> ((String)x).toUpperCase()),
                        Collections.singletonMap("merge", (x,y) -> ((String)x) + ((String)y)));
    }

    @Test
    public void itSplitsAnExpressionCorrectlyOnTheComma(){
        assertThat(expressionParserUnderTest.getIndexOfCommaSeparator("merge(toUpper('a',merge('b','c')),'d')")).isEqualTo(33);
        assertThat(expressionParserUnderTest.getIndexOfCommaSeparator("merge(toUpper('a'),merge('b',merge('c','d')))")).isEqualTo(18);
    }

    @Test
    public void itWorks(){
        assertThat(expressionParserUnderTest.parseAndBuildFunction("merge(toUpper(statusCode()),merge('b','c'))").apply(null)).isEqualTo("HELPbc");
    }
}
