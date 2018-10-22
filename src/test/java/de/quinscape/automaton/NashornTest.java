package de.quinscape.automaton;

import org.junit.Ignore;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class NashornTest
{
    @Test
    @Ignore
    public void name() throws FileNotFoundException, ScriptException
    {

        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

        engine.eval(new FileReader("./src/main/resources/prettier.js"));
    }
}
