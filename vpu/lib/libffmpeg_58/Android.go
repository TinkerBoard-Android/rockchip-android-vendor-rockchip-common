package libffmpeg_58

import (
        "android/soong/android"
        "android/soong/cc"
        "fmt"
        "strings"
)

func init() {
  //该打印会在执行mm命令时，打印在屏幕上
  fmt.Println("libffmpeg_58 want to conditional Compile")
  android.RegisterModuleType("cc_libffmpeg_58_prebuilt_library_shared", DefaultsFactory)
}

func DefaultsFactory() (android.Module) {
// 要获取对应module的factory，这一步很重要
    module := cc.PrebuiltSharedLibraryFactory()
    android.AddLoadHook(module, Defaults)
    return module
}

func Defaults(ctx android.LoadHookContext) {
    type props struct {
        Srcs []string
        Vendor  *bool
    }
    var srcs []string
    var vendor bool
    if (strings.Contains(ctx.AConfig().Getenv("TARGET_PRODUCT"),"box")) {
        srcs = append(srcs,"libffmpeg_58.so")
        vendor = true
//        fmt.Println("is box!!!!")
    } else {
        srcs = append(srcs,"libffmpeg_58.so")
        vendor = false
    }

    p := &props{}
    p.Srcs = srcs
    p.Vendor = &vendor
    ctx.AppendProperties(p)
}
