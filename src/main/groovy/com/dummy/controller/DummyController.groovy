package com.dummy.controller

import com.dummy.service.ReactiveService
import groovy.util.logging.Slf4j
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn

import io.reactivex.Flowable
import jakarta.inject.Inject
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

import static io.micronaut.http.HttpResponse.ok

@Slf4j
@Controller("/testing")
class DummyController {

    @Inject
    ReactiveService reactiveService

    @ExecuteOn(TaskExecutors.IO)
    @Get(uri = "outer")
    HttpResponse outer() {
        reactiveService.callInner()
        ok()
    }

    @Post(uri = "inner")
    @ExecuteOn(TaskExecutors.IO)
    HttpResponse inner(@Body Flowable<Integer> template){

        template.subscribe(new Subscriber<Integer>() {
            Subscription subscription

            @Override
            void onSubscribe(Subscription s) {
                subscription = s
                subscription.request(1l)
            }

            @Override
            void onNext(Integer integer) {
                log.info("inner:onNext called")
                log.info("${integer.toString()} - ${System.currentTimeMillis()}")
                subscription.request(1l)
            }

            @Override
            void onError(Throwable t) {
                log.info("inner:onError called")
                t.printStackTrace()
            }

            @Override
            void onComplete() {
                log.info("inner:onComplete called")
            }
        })
        ok()
    }
}
