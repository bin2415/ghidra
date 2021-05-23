/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ghidra.app.decompiler;

import java.io.InputStream;
import org.xml.sax.*;

import ghidra.xml.XmlPullParser;
import ghidra.xml.XmlPullParserFactory;
import ghidra.xml.XmlElement;
import ghidra.util.Msg;
/**
 * Class for represent integer value ranges.
 * 
 * The idea is to have a representation of common sets of values
 * that a varnode might take on in analysis so that the representation
 * can be manipulated symbolicaaly to some extent. The representation is
 * a circular range (determined by a half-open interval [left, right)),,
 * over the integers mode 2^n, where mask = 2^n-1.
 * The range can support a step, if some of the least significant bits
 * of the mask are set to zero.
 * 
 */

public class CircleRange {
    private long left;
    private long right;
    private long step;
    private long mask;
    private boolean isfull;
    private boolean isempty;

    public CircleRange(InputStream raw) {

        if (raw == null) {
            this.isempty = true;
        }
        XmlPullParser parser = null;
        try{
            parser = XmlPullParserFactory.create(raw, 
            "Range Analyzer Result Parser", CircleRange.getErrorHandler(this, "range analyzer"), false);
        } catch (Exception ex) {
            this.isempty = true;
            return;
        }

        XmlElement el = parser.start("circleRange");
        String leftStr = null;
        String rightStr = null;
        String stepStr = null;
        String maskStr = null;

        if (el == null) {
            this.isempty = true;
            return;
        }

        leftStr = el.getAttribute("left");
        rightStr = el.getAttribute("right");
        stepStr = el.getAttribute("step");
        maskStr = el.getAttribute("mask");

        if (leftStr == null || rightStr == null || stepStr == null) {
            this.isempty = true;
            return;
        }

        this.left = Long.parseUnsignedLong(leftStr);
        this.right = Long.parseUnsignedLong(rightStr);
        this.step = Long.parseUnsignedLong(stepStr);
        this.mask = Long.parseUnsignedLong(maskStr);
        this.isempty = false;
        
        if (this.left == this.right) {
            this.isfull = true;
        } else {
            this.isfull = false;
        }

    }

    public CircleRange(long l, long r, long s, long m) {
        this.left = l;
        this.right = r;
        this.step = s;
        this.mask = m;
        if (l == r) {
            this.isfull = true;
        } else {
            this.isfull = false;
        }
        this.isempty = false;
    }

    public CircleRange(CircleRange c2) {
        this.left = c2.left;
        this.right = c2.right;
        this.step = c2.step;
        this.mask = c2.mask;
        this.isempty = c2.isempty;
        this.isfull = c2.isfull;
    }

    public static ErrorHandler getErrorHandler(final Object errorOriginator,
            final String targetName) {
                return new ErrorHandler() {

                    @Override
                    public void error(SAXParseException exception) throws SAXException {
                        Msg.error(errorOriginator, "Error parseing " + targetName, exception);
                    }

                    @Override
                    public void fatalError(SAXParseException exception) throws SAXException {
                        Msg.error(errorOriginator, "Fatal error parsing " + targetName, exception);
                    }

                    @Override
                    public void warning(SAXParseException exception) throws SAXException {
                        Msg.warn(errorOriginator, "Warning parsing " + targetName, exception);
                    }
                    
                };
            }

    public boolean isFull() {
        return (this.isfull || this.left == this.right);
    }

    public String toString() {
        String result = "";
        if (this.isFull()) {
            result += "[Full]";
            return result;
        }
        
        result += "[";
        result += Long.toHexString(this.left);
        result += ",";
        result += Long.toHexString(this.right);
        result += ",";
        result += Long.toHexString(this.step);
        result += ")";

        return result;
    }
}

