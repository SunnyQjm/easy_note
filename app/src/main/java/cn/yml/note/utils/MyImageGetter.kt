//package cn.yml.note.utils
//
//import android.graphics.drawable.Drawable
//import android.widget.TextView
//import cn.yml.note.GlideApp
//import cn.yml.note.R
//import com.zzhoujay.richtext.ImageHolder
//import com.zzhoujay.richtext.RichTextConfig
//import com.zzhoujay.richtext.callback.ImageGetter
//import com.zzhoujay.richtext.callback.ImageLoadNotify
//import com.zzhoujay.richtext.drawable.DrawableWrapper
//import com.zzhoujay.richtext.ext.Base64
//import com.bumptech.glide.load.resource.bitmap.TransformationUtils.cfitCenter
//import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy.FIT_CENTER
//import com.bumptech.glide.load.resource.bitmap.TransformationUtils.centerCrop
//
//
//class MyImageGetter : ImageGetter, ImageLoadNotify {
//
//
//    private var imageLoadNotify: ImageLoadNotify? = null
//
//    override fun registerImageLoadNotify(imageLoadNotify: ImageLoadNotify?) {
//        this.imageLoadNotify = imageLoadNotify
//    }
//
//    override fun getDrawable(holder: ImageHolder?, config: RichTextConfig?, textView: TextView?): Drawable {
//        val drawableWrapper = DrawableWrapper(holder)
//
//        textView?.let {
//            config?.let {
//                holder?.let {
//                    val src = Base64.decode(holder.source)
//                    val grd = GlideApp.with(textView.context)
//                        .load(src)
//                    when (holder.scaleType) {
//                        ImageHolder.ScaleType.fit_center -> {
//                            grd.fitCenter()
//                        }
//                        ImageHolder.ScaleType.center_crop -> {
//                            grd.centerCrop()
//                        }
//                        ImageHolder.ScaleType.center_inside -> {
//                            grd.centerInside()
//                        }
//                        else -> {
//
//                        }
//                    }
//
//                    textView.post {
//                        grd.placeholder(R.drawable.img_empty)
////                            .into()
//                    }
//                }
//            }
//        }
//        return drawableWrapper
//    }
//
//    override fun done(from: Any?) {
//
//    }
//
//    override fun recycle() {
//    }
//
//}