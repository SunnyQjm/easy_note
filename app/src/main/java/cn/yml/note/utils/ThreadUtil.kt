package cn.yml.note.utils

import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import java.util.concurrent.TimeUnit

/**
 * Created by sunny on 18-1-26.
 */

/**
 * 执行异步操作
 */
fun doAsync(runnable: () -> Unit) {
    Thread(Runnable {
        runnable()
    }).start()
}

fun doInMainThread(work: () -> Unit) {
    Observable.just(0)
            .compose(RxSchedulersHelper.io_main())
            .subscribe({
                work()
            })
}

fun <T> doAsyncObserMain(work: () -> T, callback: (T) -> Unit) {
    Observable.create(ObservableOnSubscribe<T> {
        it.onNext(work())
    }).compose(RxSchedulersHelper.io_main())
            .subscribe {
                callback(it)
            }
}

fun doInterval(start: Long = 0L, count: Long = 0L, initialDelay: Long = 0L,
               period: Long = 1L, timeUnit: TimeUnit = TimeUnit.SECONDS, work: (count: Long) -> Unit) {
    Observable.intervalRange(start, count, initialDelay, period, timeUnit)
            .compose(RxSchedulersHelper.io_main())
            .subscribe {
                work(it)
            }
}