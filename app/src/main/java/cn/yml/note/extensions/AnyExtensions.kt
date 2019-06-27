package cn.yml.note.extensions

import cn.yml.note.utils.GsonUtil

fun Any.toJson(): String {
    return GsonUtil.bean2Json(this) ?: ""
}