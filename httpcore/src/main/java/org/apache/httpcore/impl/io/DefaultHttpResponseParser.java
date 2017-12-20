/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.httpcore.impl.io;

import java.io.IOException;

import org.apache.httpcore.HttpException;
import org.apache.httpcore.HttpResponse;
import org.apache.httpcore.HttpResponseFactory;
import org.apache.httpcore.NoHttpResponseException;
import org.apache.httpcore.ParseException;
import org.apache.httpcore.StatusLine;
import org.apache.httpcore.config.MessageConstraints;
import org.apache.httpcore.impl.DefaultHttpResponseFactory;
import org.apache.httpcore.io.SessionInputBuffer;
import org.apache.httpcore.message.BasicLineParser;
import org.apache.httpcore.message.LineParser;
import org.apache.httpcore.message.ParserCursor;
import org.apache.httpcore.params.HttpParams;
import org.apache.httpcore.util.Args;
import org.apache.httpcore.util.CharArrayBuffer;

/**
 * HTTP response parser that obtain its input from an instance
 * of {@link SessionInputBuffer}.
 *
 * @since 4.2
 */
@SuppressWarnings("deprecation")
public class DefaultHttpResponseParser extends AbstractMessageParser<HttpResponse> {

    private final HttpResponseFactory responseFactory;
    private final CharArrayBuffer lineBuf;

    /**
     * Creates an instance of this class.
     *
     * @param buffer the session input buffer.
     * @param lineParser the line parser.
     * @param responseFactory the factory to use to create
     *    {@link HttpResponse}s.
     * @param params HTTP parameters.
     *
     * @deprecated (4.3) use
     *   {@link DefaultHttpResponseParser#DefaultHttpResponseParser(SessionInputBuffer, LineParser,
     *     HttpResponseFactory, MessageConstraints)}
     */
    @Deprecated
    public DefaultHttpResponseParser(
            final SessionInputBuffer buffer,
            final LineParser lineParser,
            final HttpResponseFactory responseFactory,
            final HttpParams params) {
        super(buffer, lineParser, params);
        this.responseFactory = Args.notNull(responseFactory, "Response factory");
        this.lineBuf = new CharArrayBuffer(128);
    }

    /**
     * Creates new instance of DefaultHttpResponseParser.
     *
     * @param buffer the session input buffer.
     * @param lineParser the line parser. If {@code null}
     *   {@link BasicLineParser#INSTANCE} will be used
     * @param responseFactory the response factory. If {@code null}
     *   {@link DefaultHttpResponseFactory#INSTANCE} will be used.
     * @param constraints the message constraints. If {@code null}
     *   {@link MessageConstraints#DEFAULT} will be used.
     *
     * @since 4.3
     */
    public DefaultHttpResponseParser(
            final SessionInputBuffer buffer,
            final LineParser lineParser,
            final HttpResponseFactory responseFactory,
            final MessageConstraints constraints) {
        super(buffer, lineParser, constraints);
        this.responseFactory = responseFactory != null ? responseFactory :
            DefaultHttpResponseFactory.INSTANCE;
        this.lineBuf = new CharArrayBuffer(128);
    }

    /**
     * @since 4.3
     */
    public DefaultHttpResponseParser(
            final SessionInputBuffer buffer,
            final MessageConstraints constraints) {
        this(buffer, null, null, constraints);
    }

    /**
     * @since 4.3
     */
    public DefaultHttpResponseParser(final SessionInputBuffer buffer) {
        this(buffer, null, null, MessageConstraints.DEFAULT);
    }

    @Override
    protected HttpResponse parseHead(
            final SessionInputBuffer sessionBuffer)
        throws IOException, HttpException, ParseException {

        this.lineBuf.clear();
        final int i = sessionBuffer.readLine(this.lineBuf);
        if (i == -1) {
            throw new NoHttpResponseException("The target server failed to respond");
        }
        //create the status line from the status string
        final ParserCursor cursor = new ParserCursor(0, this.lineBuf.length());
        final StatusLine statusline = lineParser.parseStatusLine(this.lineBuf, cursor);
        return this.responseFactory.newHttpResponse(statusline, null);
    }

}
