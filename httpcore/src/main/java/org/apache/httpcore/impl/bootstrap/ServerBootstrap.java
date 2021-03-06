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
package org.apache.httpcore.impl.bootstrap;

import org.apache.httpcore.ConnectionReuseStrategy;
import org.apache.httpcore.ExceptionLogger;
import org.apache.httpcore.HttpConnectionFactory;
import org.apache.httpcore.HttpRequestInterceptor;
import org.apache.httpcore.HttpResponseFactory;
import org.apache.httpcore.HttpResponseInterceptor;
import org.apache.httpcore.config.ConnectionConfig;
import org.apache.httpcore.config.SocketConfig;
import org.apache.httpcore.impl.DefaultBHttpServerConnection;
import org.apache.httpcore.impl.DefaultBHttpServerConnectionFactory;
import org.apache.httpcore.impl.DefaultConnectionReuseStrategy;
import org.apache.httpcore.impl.DefaultHttpResponseFactory;
import org.apache.httpcore.protocol.HttpExpectationVerifier;
import org.apache.httpcore.protocol.HttpProcessor;
import org.apache.httpcore.protocol.HttpProcessorBuilder;
import org.apache.httpcore.protocol.HttpRequestHandler;
import org.apache.httpcore.protocol.HttpRequestHandlerMapper;
import org.apache.httpcore.protocol.HttpService;
import org.apache.httpcore.protocol.ResponseConnControl;
import org.apache.httpcore.protocol.ResponseContent;
import org.apache.httpcore.protocol.ResponseDate;
import org.apache.httpcore.protocol.ResponseServer;
import org.apache.httpcore.protocol.UriHttpRequestHandlerMapper;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLContext;

/**
 * @since 4.4
 */
public class ServerBootstrap {

    private int listenerPort;
    private InetAddress localAddress;
    private SocketConfig socketConfig;
    private ConnectionConfig connectionConfig;
    private LinkedList<HttpRequestInterceptor> requestFirst;
    private LinkedList<HttpRequestInterceptor> requestLast;
    private LinkedList<HttpResponseInterceptor> responseFirst;
    private LinkedList<HttpResponseInterceptor> responseLast;
    private String serverInfo;
    private HttpProcessor httpProcessor;
    private ConnectionReuseStrategy connStrategy;
    private HttpResponseFactory responseFactory;
    private HttpRequestHandlerMapper handlerMapper;
    private Map<String, HttpRequestHandler> handlerMap;
    private HttpExpectationVerifier expectationVerifier;
    private ServerSocketFactory serverSocketFactory;
    private SSLContext sslContext;
    private SSLServerSetupHandler sslSetupHandler;
    private HttpConnectionFactory<? extends DefaultBHttpServerConnection> connectionFactory;
    private ExceptionLogger exceptionLogger;

    private ServerBootstrap() {
    }

    public static ServerBootstrap bootstrap() {
        return new ServerBootstrap();
    }

    /**
     * Sets listener port number.
     *
     * @return this
     */
    public final ServerBootstrap setListenerPort(final int listenerPort) {
        this.listenerPort = listenerPort;
        return this;
    }

    /**
     * Assigns local interface for the listener.
     *
     * @return this
     */
    public final ServerBootstrap setLocalAddress(final InetAddress localAddress) {
        this.localAddress = localAddress;
        return this;
    }

    /**
     * Sets socket configuration.
     *
     * @return this
     */
    public final ServerBootstrap setSocketConfig(final SocketConfig socketConfig) {
        this.socketConfig = socketConfig;
        return this;
    }

    /**
     * Sets connection configuration. <p> Please note this value can be overridden by the {@link
     * #setConnectionFactory(org.apache.httpcore.HttpConnectionFactory)} method. </p>
     *
     * @return this
     */
    public final ServerBootstrap setConnectionConfig(final ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
        return this;
    }

    /**
     * Assigns {@link HttpProcessor} instance.
     *
     * @return this
     */
    public final ServerBootstrap setHttpProcessor(final HttpProcessor httpProcessor) {
        this.httpProcessor = httpProcessor;
        return this;
    }

    /**
     * Adds this protocol interceptor to the head of the protocol processing list. <p> Please note this value
     * can be overridden by the {@link #setHttpProcessor(org.apache.httpcore.protocol.HttpProcessor)} method.
     * </p>
     *
     * @return this
     */
    public final ServerBootstrap addInterceptorFirst(final HttpResponseInterceptor itcp) {
        if (itcp == null) {
            return this;
        }
        if (responseFirst == null) {
            responseFirst = new LinkedList<HttpResponseInterceptor>();
        }
        responseFirst.addFirst(itcp);
        return this;
    }

    /**
     * Adds this protocol interceptor to the tail of the protocol processing list. <p> Please note this value
     * can be overridden by the {@link #setHttpProcessor(org.apache.httpcore.protocol.HttpProcessor)} method.
     * </p>
     *
     * @return this
     */
    public final ServerBootstrap addInterceptorLast(final HttpResponseInterceptor itcp) {
        if (itcp == null) {
            return this;
        }
        if (responseLast == null) {
            responseLast = new LinkedList<HttpResponseInterceptor>();
        }
        responseLast.addLast(itcp);
        return this;
    }

    /**
     * Adds this protocol interceptor to the head of the protocol processing list. <p> Please note this value
     * can be overridden by the {@link #setHttpProcessor(org.apache.httpcore.protocol.HttpProcessor)} method.
     * </p>
     *
     * @return this
     */
    public final ServerBootstrap addInterceptorFirst(final HttpRequestInterceptor itcp) {
        if (itcp == null) {
            return this;
        }
        if (requestFirst == null) {
            requestFirst = new LinkedList<HttpRequestInterceptor>();
        }
        requestFirst.addFirst(itcp);
        return this;
    }

    /**
     * Adds this protocol interceptor to the tail of the protocol processing list. <p> Please note this value
     * can be overridden by the {@link #setHttpProcessor(org.apache.httpcore.protocol.HttpProcessor)} method.
     * </p>
     *
     * @return this
     */
    public final ServerBootstrap addInterceptorLast(final HttpRequestInterceptor itcp) {
        if (itcp == null) {
            return this;
        }
        if (requestLast == null) {
            requestLast = new LinkedList<HttpRequestInterceptor>();
        }
        requestLast.addLast(itcp);
        return this;
    }

    /**
     * Assigns {@code Server} response header value. <p> Please note this value can be overridden by the
     * {@link #setHttpProcessor(org.apache.httpcore.protocol.HttpProcessor)} method. </p>
     *
     * @return this
     */
    public final ServerBootstrap setServerInfo(final String serverInfo) {
        this.serverInfo = serverInfo;
        return this;
    }

    /**
     * Assigns {@link ConnectionReuseStrategy} instance.
     *
     * @return this
     */
    public final ServerBootstrap setConnectionReuseStrategy(final ConnectionReuseStrategy connStrategy) {
        this.connStrategy = connStrategy;
        return this;
    }

    /**
     * Assigns {@link HttpResponseFactory} instance.
     *
     * @return this
     */
    public final ServerBootstrap setResponseFactory(final HttpResponseFactory responseFactory) {
        this.responseFactory = responseFactory;
        return this;
    }

    /**
     * Assigns {@link HttpRequestHandlerMapper} instance.
     *
     * @return this
     */
    public final ServerBootstrap setHandlerMapper(final HttpRequestHandlerMapper handlerMapper) {
        this.handlerMapper = handlerMapper;
        return this;
    }

    /**
     * Registers the given {@link HttpRequestHandler} as a handler for URIs matching the given pattern. <p>
     * Please note this value can be overridden by the {@link #setHandlerMapper(
     *org.apache.httpcore.protocol.HttpRequestHandlerMapper)} method. </p>
     *
     * @param pattern the pattern to register the handler for.
     * @param handler the handler.
     *
     * @return this
     */
    public final ServerBootstrap registerHandler(final String pattern, final HttpRequestHandler handler) {
        if (pattern == null || handler == null) {
            return this;
        }
        if (handlerMap == null) {
            handlerMap = new HashMap<String, HttpRequestHandler>();
        }
        handlerMap.put(pattern, handler);
        return this;
    }

    /**
     * Assigns {@link HttpExpectationVerifier} instance.
     *
     * @return this
     */
    public final ServerBootstrap setExpectationVerifier(final HttpExpectationVerifier expectationVerifier) {
        this.expectationVerifier = expectationVerifier;
        return this;
    }

    /**
     * Assigns {@link HttpConnectionFactory} instance.
     *
     * @return this
     */
    public final ServerBootstrap setConnectionFactory(
      final HttpConnectionFactory<? extends DefaultBHttpServerConnection> connectionFactory) {
        this.connectionFactory = connectionFactory;
        return this;
    }

    /**
     * Assigns {@link org.apache.httpcore.impl.bootstrap.SSLServerSetupHandler} instance.
     *
     * @return this
     */
    public final ServerBootstrap setSslSetupHandler(final SSLServerSetupHandler sslSetupHandler) {
        this.sslSetupHandler = sslSetupHandler;
        return this;
    }

    /**
     * Assigns {@link ServerSocketFactory} instance.
     *
     * @return this
     */
    public final ServerBootstrap setServerSocketFactory(final ServerSocketFactory serverSocketFactory) {
        this.serverSocketFactory = serverSocketFactory;
        return this;
    }

    /**
     * Assigns {@link SSLContext} instance. <p> Please note this value can be overridden by the {@link
     * #setServerSocketFactory(ServerSocketFactory)} method. </p>
     *
     * @return this
     */
    public final ServerBootstrap setSslContext(final SSLContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

    /**
     * Assigns {@link org.apache.httpcore.ExceptionLogger} instance.
     *
     * @return this
     */
    public final ServerBootstrap setExceptionLogger(final ExceptionLogger exceptionLogger) {
        this.exceptionLogger = exceptionLogger;
        return this;
    }

    public HttpServer create() {

        HttpProcessor httpProcessorCopy = this.httpProcessor;
        if (httpProcessorCopy == null) {

            final HttpProcessorBuilder b = HttpProcessorBuilder.create();
            if (requestFirst != null) {
                for (final HttpRequestInterceptor i : requestFirst) {
                    b.addFirst(i);
                }
            }
            if (responseFirst != null) {
                for (final HttpResponseInterceptor i : responseFirst) {
                    b.addFirst(i);
                }
            }

            String serverInfoCopy = this.serverInfo;
            if (serverInfoCopy == null) {
                serverInfoCopy = "Apache-HttpCore/1.1";
            }

            b.addAll(new ResponseDate(), new ResponseServer(serverInfoCopy), new ResponseContent(),
              new ResponseConnControl());
            if (requestLast != null) {
                for (final HttpRequestInterceptor i : requestLast) {
                    b.addLast(i);
                }
            }
            if (responseLast != null) {
                for (final HttpResponseInterceptor i : responseLast) {
                    b.addLast(i);
                }
            }
            httpProcessorCopy = b.build();
        }

        HttpRequestHandlerMapper handlerMapperCopy = this.handlerMapper;
        if (handlerMapperCopy == null) {
            final UriHttpRequestHandlerMapper reqistry = new UriHttpRequestHandlerMapper();
            if (handlerMap != null) {
                for (final Map.Entry<String, HttpRequestHandler> entry : handlerMap.entrySet()) {
                    reqistry.register(entry.getKey(), entry.getValue());
                }
            }
            handlerMapperCopy = reqistry;
        }

        ConnectionReuseStrategy connStrategyCopy = this.connStrategy;
        if (connStrategyCopy == null) {
            connStrategyCopy = DefaultConnectionReuseStrategy.INSTANCE;
        }

        HttpResponseFactory responseFactoryCopy = this.responseFactory;
        if (responseFactoryCopy == null) {
            responseFactoryCopy = DefaultHttpResponseFactory.INSTANCE;
        }

        final HttpService httpService =
          new HttpService(httpProcessorCopy, connStrategyCopy, responseFactoryCopy, handlerMapperCopy,
            this.expectationVerifier);

        ServerSocketFactory serverSocketFactoryCopy = this.serverSocketFactory;
        if (serverSocketFactoryCopy == null) {
            if (this.sslContext != null) {
                serverSocketFactoryCopy = this.sslContext.getServerSocketFactory();
            } else {
                serverSocketFactoryCopy = ServerSocketFactory.getDefault();
            }
        }

        HttpConnectionFactory<? extends DefaultBHttpServerConnection> connectionFactoryCopy =
          this.connectionFactory;
        if (connectionFactoryCopy == null) {
            if (this.connectionConfig != null) {
                connectionFactoryCopy = new DefaultBHttpServerConnectionFactory(this.connectionConfig);
            } else {
                connectionFactoryCopy = DefaultBHttpServerConnectionFactory.INSTANCE;
            }
        }

        ExceptionLogger exceptionLoggerCopy = this.exceptionLogger;
        if (exceptionLoggerCopy == null) {
            exceptionLoggerCopy = ExceptionLogger.NO_OP;
        }

        return new HttpServer(this.listenerPort > 0 ? this.listenerPort : 0, this.localAddress,
          this.socketConfig != null ? this.socketConfig : SocketConfig.DEFAULT, serverSocketFactoryCopy,
          httpService, connectionFactoryCopy, this.sslSetupHandler, exceptionLoggerCopy);
    }

}