/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl;

import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.PromiseInternal;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.mssqlclient.MSSQLConnection;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.SqlConnectionImpl;
import io.vertx.sqlclient.impl.tracing.SqlTracer;

public class MSSQLConnectionImpl extends SqlConnectionImpl<MSSQLConnectionImpl> implements MSSQLConnection {
  private final MSSQLConnectionFactory factory;

  public MSSQLConnectionImpl(MSSQLConnectionFactory factory, ContextInternal context, Connection conn, SqlTracer tracer) {
    super(context, conn, tracer);
    this.factory = factory;
  }

  @Override
  public int appendQueryPlaceholder(StringBuilder queryBuilder, int index, int current) {
    queryBuilder.append('@').append('P').append(1 + index);
    return index;
  }

  public static Future<MSSQLConnection> connect(Vertx vertx, MSSQLConnectOptions options) {
    ContextInternal ctx = (ContextInternal) vertx.getOrCreateContext();
    SqlTracer tracer = ctx.tracer() == null ? null : new SqlTracer(ctx.tracer(), options);
    PromiseInternal<MSSQLConnection> promise = ctx.promise();
    MSSQLConnectionFactory client = new MSSQLConnectionFactory(ctx, options);
    ctx.dispatch(null, v -> {
      client.connect()
        .<MSSQLConnection>map(conn -> {
          MSSQLConnectionImpl msConn = new MSSQLConnectionImpl(client, ctx, conn, tracer);
          conn.init(msConn);
          return msConn;
        }).onComplete(promise);
    });
    return promise.future();
  }
}
