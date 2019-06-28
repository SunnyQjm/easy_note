package cn.yml.note.model

import cn.bmob.v3.BmobUser

data class User(
    var avatar: String = ""
): BmobUser()