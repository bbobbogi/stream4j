/**
 * Chzzk (NAVER) streaming platform integration.
 *
 * <p>Provides chat, donation, and channel/live information support for the
 * <a href="https://chzzk.naver.com">Chzzk</a> platform.
 *
 * <p>Primary entry points:
 * <ul>
 *   <li>{@link io.github.bbobbogi.stream4j.chzzk.ChzzkBuilder} &mdash; Build a Chzzk API client with optional NAVER authentication</li>
 *   <li>{@link io.github.bbobbogi.stream4j.chzzk.Chzzk} &mdash; Query channel info, live status, and create chat connections</li>
 *   <li>{@link io.github.bbobbogi.stream4j.chzzk.ChzzkChat} &mdash; WebSocket-based real-time chat client</li>
 * </ul>
 *
 * <p>All public types in this package are part of the stable API unless noted otherwise.
 *
 * @since 1.0.0
 */
package io.github.bbobbogi.stream4j.chzzk;
