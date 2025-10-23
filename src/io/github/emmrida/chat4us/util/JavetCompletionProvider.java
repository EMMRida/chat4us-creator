/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.util;

import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;

/**
 * A simple completion provider for Script text area.
 *
 * @author El Mhadder Mohamed Rida
 */
public class JavetCompletionProvider extends DefaultCompletionProvider {

	/**
	 * Init this instance.
	 */
    public JavetCompletionProvider() {
        loadJavaScriptCompletions();
    }

    /**
     * Loads most used JS functions for auto completion.
     */
    private void loadJavaScriptCompletions() {
        // Add JavaScript keywords
        addCompletion(new ShorthandCompletion(this, "function", "function name() {\n\t\n}", "function"));
        addCompletion(new ShorthandCompletion(this, "if", "if (condition) {\n\t\n}", "if"));
        addCompletion(new ShorthandCompletion(this, "else", "else {\n\t\n}", "else"));
        addCompletion(new ShorthandCompletion(this, "for", "for (let i = 0; i < n; i++) {\n\t\n}", "for"));
        addCompletion(new ShorthandCompletion(this, "while", "while (condition) {\n\t\n}", "while"));
        addCompletion(new ShorthandCompletion(this, "do", "do {\n\t\n} while (condition);", "do-while loop"));
        addCompletion(new ShorthandCompletion(this, "switch", "switch (expression) {\n\tcase value:\n\t\tbreak;\n\tdefault:\n\t\tbreak;\n}", "switch statement"));
        addCompletion(new ShorthandCompletion(this, "return", "return value;", "return statement"));
        addCompletion(new ShorthandCompletion(this, "var", "var name = value;", "variable declaration"));
        addCompletion(new ShorthandCompletion(this, "let", "let name = value;", "block-scoped variable"));
        addCompletion(new ShorthandCompletion(this, "const", "const name = value;", "constant declaration"));
        addCompletion(new ShorthandCompletion(this, "try", "try {\n\t\n} catch (error) {\n\t\n}", "try-catch block"));
        addCompletion(new ShorthandCompletion(this, "catch", "catch (error) {\n\t\n}", "catch block"));
        addCompletion(new ShorthandCompletion(this, "throw", "throw new Error('message');", "throw an error"));
        addCompletion(new ShorthandCompletion(this, "class", "class Name {\n\tconstructor() {\n\t\t\n\t}\n}", "class declaration"));
        addCompletion(new ShorthandCompletion(this, "new", "new ClassName();", "create an instance"));
        addCompletion(new ShorthandCompletion(this, "this", "this.property", "reference to current object"));
        addCompletion(new ShorthandCompletion(this, "super", "super();", "call parent constructor"));

        // Add JavaScript built-in functions
        addCompletion(new ShorthandCompletion(this, "parseInt", "parseInt(string);", "parse string to integer"));
        addCompletion(new ShorthandCompletion(this, "parseFloat", "parseFloat(string);", "parse string to float"));
        addCompletion(new ShorthandCompletion(this, "isNaN", "isNaN(value);", "check if value is NaN"));
        addCompletion(new ShorthandCompletion(this, "isFinite", "isFinite(value);", "check if value is finite"));
        addCompletion(new ShorthandCompletion(this, "eval", "eval(code);", "evaluate JavaScript code"));

        // Add JavaScript global objects and methods
        addCompletion(new ShorthandCompletion(this, "Math.abs", "Math.abs(x);", "absolute value"));
        addCompletion(new ShorthandCompletion(this, "Math.random", "Math.random();", "random number between 0 and 1"));
        addCompletion(new ShorthandCompletion(this, "Math.floor", "Math.floor(x);", "round down to nearest integer"));
        addCompletion(new ShorthandCompletion(this, "Math.ceil", "Math.ceil(x);", "round up to nearest integer"));
        addCompletion(new ShorthandCompletion(this, "Math.round", "Math.round(x);", "round to nearest integer"));
        addCompletion(new ShorthandCompletion(this, "Math.max", "Math.max(x, y);", "maximum of two numbers"));
        addCompletion(new ShorthandCompletion(this, "Math.min", "Math.min(x, y);", "minimum of two numbers"));
        addCompletion(new ShorthandCompletion(this, "Math.pow", "Math.pow(x, y);", "x raised to the power of y"));
        addCompletion(new ShorthandCompletion(this, "Math.sqrt", "Math.sqrt(x);", "square root of x"));
        addCompletion(new ShorthandCompletion(this, "Date.now", "Date.now();", "current timestamp"));
        addCompletion(new ShorthandCompletion(this, "Date.parse", "Date.parse(dateString);", "parse date string"));
        addCompletion(new ShorthandCompletion(this, "Date.UTC", "Date.UTC(year, month, day);", "get UTC timestamp"));

        // Add JSON methods
        addCompletion(new ShorthandCompletion(this, "JSON.stringify", "JSON.stringify(object);", "convert object to JSON string"));
        addCompletion(new ShorthandCompletion(this, "JSON.parse", "JSON.parse(string);", "parse JSON string to object"));

        // Add Array methods
        addCompletion(new ShorthandCompletion(this, "Array.push", "push(item);", "add item to end of array"));
        addCompletion(new ShorthandCompletion(this, "Array.pop", "pop();", "remove last item from array"));
        addCompletion(new ShorthandCompletion(this, "Array.shift", "shift();", "remove first item from array"));
        addCompletion(new ShorthandCompletion(this, "Array.unshift", "unshift(item);", "add item to beginning of array"));
        addCompletion(new ShorthandCompletion(this, "Array.slice", "slice(start, end);", "extract portion of array"));
        addCompletion(new ShorthandCompletion(this, "Array.splice", "splice(start, deleteCount, item1, item2);", "add/remove items from array"));
        addCompletion(new ShorthandCompletion(this, "Array.forEach", "forEach(function(item) {\n\t\n});", "execute function for each item"));
        addCompletion(new ShorthandCompletion(this, "Array.map", "map(function(item) {\n\treturn value;\n});", "create new array by mapping items"));
        addCompletion(new ShorthandCompletion(this, "Array.filter", "filter(function(item) {\n\treturn condition;\n});", "create new array by filtering items"));
        addCompletion(new ShorthandCompletion(this, "Array.reduce", "reduce(function(accumulator, item) {\n\treturn value;\n}, initialValue);", "reduce array to a single value"));
        addCompletion(new ShorthandCompletion(this, "Array.find", "find(function(item) {\n\treturn condition;\n});", "find first item matching condition"));
        addCompletion(new ShorthandCompletion(this, "Array.findIndex", "findIndex(function(item) {\n\treturn condition;\n});", "find index of first item matching condition"));
        addCompletion(new ShorthandCompletion(this, "Array.includes", "includes(item);", "check if array includes item"));
        addCompletion(new ShorthandCompletion(this, "Array.indexOf", "indexOf(item);", "find index of item"));
        addCompletion(new ShorthandCompletion(this, "Array.join", "join(separator);", "join array elements into a string"));
        addCompletion(new ShorthandCompletion(this, "Array.reverse", "reverse();", "reverse array elements"));
        addCompletion(new ShorthandCompletion(this, "Array.sort", "sort(function(a, b) {\n\treturn a - b;\n});", "sort array elements"));

        // Add String methods
        addCompletion(new ShorthandCompletion(this, "String.charAt", "charAt(index);", "get character at index"));
        addCompletion(new ShorthandCompletion(this, "String.substring", "substring(start, end);", "extract substring"));
        addCompletion(new ShorthandCompletion(this, "String.indexOf", "indexOf(searchValue);", "find index of substring"));
        addCompletion(new ShorthandCompletion(this, "String.replace", "replace(searchValue, replaceValue);", "replace substring"));
        addCompletion(new ShorthandCompletion(this, "String.split", "split(separator);", "split string into array"));
        addCompletion(new ShorthandCompletion(this, "String.trim", "trim();", "remove whitespace from ends"));
        addCompletion(new ShorthandCompletion(this, "String.startsWith", "startsWith(searchValue);", "check if string starts with value"));
        addCompletion(new ShorthandCompletion(this, "String.endsWith", "endsWith(searchValue);", "check if string ends with value"));
        addCompletion(new ShorthandCompletion(this, "String.includes", "includes(searchValue);", "check if string includes value"));
        addCompletion(new ShorthandCompletion(this, "String.toLowerCase", "toLowerCase();", "convert string to lowercase"));
        addCompletion(new ShorthandCompletion(this, "String.toUpperCase", "toUpperCase();", "convert string to uppercase"));
        addCompletion(new ShorthandCompletion(this, "String.match", "match(regex);", "match string against regex"));
        addCompletion(new ShorthandCompletion(this, "String.replaceAll", "replaceAll(searchValue, replaceValue);", "replace all occurrences of substring"));
    }
}
