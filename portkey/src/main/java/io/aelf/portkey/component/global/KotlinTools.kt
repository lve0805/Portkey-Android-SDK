package io.aelf.portkey.component.global

import io.aelf.portkey.internal.tools.GsonProvider

internal fun toJson(obj:Any):String{
    return GsonProvider.getGson().toJson(obj)
}