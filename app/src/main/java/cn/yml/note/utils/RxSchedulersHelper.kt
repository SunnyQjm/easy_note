package cn.yml.note.utils

import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * 处理Rx线程
 * Created by Ming.J.
 */
object RxSchedulersHelper {

    fun <T> io_main(): ObservableTransformer<T, T> {
        return ObservableTransformer { upstream ->
            upstream
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun <T> io_io(): ObservableTransformer<T, T>{
        return ObservableTransformer { upstream ->
            upstream.subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
        }
    }
    fun <T> main_io(): ObservableTransformer<T, T> {
        return ObservableTransformer { upstream ->
            upstream.subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(Schedulers.io())
        }
    }

    fun <T> comppute_main(): ObservableTransformer<T, T> {
        return ObservableTransformer { upstream ->
            upstream.subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }
}

