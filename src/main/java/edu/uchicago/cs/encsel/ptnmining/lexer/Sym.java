/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License  Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an
 * "AS IS" BASIS  WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND  either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License  
 *
 * Contributors:
 *     Hao Jiang - initial API and implementation
 */

package edu.uchicago.cs.encsel.ptnmining.lexer;

/**
 * Created by harper on 3/14/17.
 */
public interface Sym {
    int INTEGER = 1;
    int DOUBLE = 2;
    int WORD = 3;
    int SPACE = 4;
    int DASH = 5;
    int UNDERSCORE = 6;
    int LPARA = 7;
    int RPARA = 8;
    int LBRAC = 9;
    int RBRAC = 10;
    int LBRAK = 11;
    int RBRAK = 12;
    int COMMA = 13;
    int PERIOD = 14;
    int COLON = 15;
    int SEMICOLON = 16;
    int SLASH = 17;
    int BACKSLASH = 18;
    int OTHER = 19;
}
