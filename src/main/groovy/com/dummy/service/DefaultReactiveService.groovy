package com.dummy.service


import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Primary
import io.micronaut.core.io.buffer.ByteBuffer
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.annotation.Client
import io.micronaut.rxjava2.http.client.RxStreamingHttpClient
import io.reactivex.Flowable

import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

@Primary
@Singleton
@Slf4j
class DefaultReactiveService implements ReactiveService {

    @Client("reactive-client")
    @Inject
    RxStreamingHttpClient client

    @Override
    void callInner() {
        Flowable<Integer> flow = Flowable.fromIterable([1, 2, 3, 4, 5])
        client.exchangeStream(HttpRequest.POST("/testing/inner", flow)).subscribe(new Subscriber<HttpResponse<ByteBuffer<?>>>() {

            Subscription subscription

            @Override
            void onSubscribe(Subscription s) {
                subscription = s
                subscription.request(1L)
            }

            @Override
            void onNext(HttpResponse<ByteBuffer<?>> byteBufferHttpResponse) {
                log.info("outer:onNext called")
                log.info(byteBufferHttpResponse.body().toString())
                subscription.request(1L)
            }

            @Override
            void onError(Throwable t) {
                log.info("outer:onError called")
                log.info(t.message)
            }

            @Override
            void onComplete() {
                log.info('outer:onComplete called')
            }
        })
    }
}
