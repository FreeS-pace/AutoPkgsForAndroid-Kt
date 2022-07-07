package com.ky.auto_pkg.model

/**
 * Created by 王金瑞
 * 2022/4/1
 * 17:55
 * com.ky.auto_pkg.model
 */
class FeiShuPostObj() {
    val post = PostChild1Obj()

    fun setTitle(title: String) {
        post.zh_cn.title = title
    }

    fun addPartData(part: ArrayList<IPartItemData>) {
        post.zh_cn.content.add(part)
    }
}

class PostChild1Obj {
    val zh_cn = PostChild2Obj()
}

class PostChild2Obj {
    var title: String? = null
    val content: ArrayList<ArrayList<IPartItemData>> = ArrayList()
}

interface IPartItemData

class PartTextObj(
    val text: String
) : IPartItemData {
    val tag: String = "text"
}

data class PartImgObj(
    val image_key: String,
    var width: Int? = 200,
    var height: Int? = 200
) : IPartItemData {
    val tag: String = "img"
}