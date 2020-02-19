package libvpu

import (
        "android/soong/android"
        "android/soong/cc"
        "fmt"
        "strings"
)

func init() {
  //该打印会在执行mm命令时，打印在屏幕上
  fmt.Println("libvpu want to conditional Compile")
  android.RegisterModuleType("cc_libvpu_prebuilt_library_shared", DefaultsFactory)
}

func DefaultsFactory() (android.Module) {
// 要获取对应module的factory，这一步很重要
    module := cc.PrebuiltSharedLibraryFactory()
    android.AddLoadHook(module, Defaults)
    return module
}

type Ex_srcs struct {
         Srcs []string
}

type Ex_multilibType struct { 
         Lib32 Ex_srcs
         Lib64 Ex_srcs
}

func Defaults(ctx android.LoadHookContext) {
    if (strings.EqualFold(ctx.AConfig().DevicePrimaryArchType().String(),"arm64")) {
    type props struct {
       Compile_multilib *string
             Multilib Ex_multilibType
    }
    p := &props{}
    p.Compile_multilib = globalCompileDefaults(ctx)
          p.Multilib = globalArm64Defaults(ctx)
    ctx.AppendProperties(p)
    } else {
          type props struct {
             Compile_multilib *string
             Srcs []string
          }
          p := &props{}
          p.Compile_multilib = globalCompileDefaults(ctx)
          p.Srcs = globalArmDefaults(ctx)
          ctx.AppendProperties(p)
    }
    
}
//条件编译主要修改函数
func globalCompileDefaults(ctx android.LoadHookContext) (*string) {
  var compile_multilib string
  //该打印输出为: TARGET_PRODUCT:rk3328 
  //fmt.Println("TARGET_PRODUCT:",ctx.AConfig().Getenv("TARGET_BOARD_PLATFORM")) //通过 strings.EqualFold 比较字符串，可参考go语言字符串对比
  //fmt.Println("TARGET_ARCH:",ctx.AConfig().DevicePrimaryArchType().String())
  target_arch := ctx.AConfig().DevicePrimaryArchType().String()
  if (strings.EqualFold(ctx.AConfig().Getenv("TARGET_BOARD_PLATFORM"),"rk3126c")) {
        compile_multilib = "32"
  } else if (strings.EqualFold(ctx.AConfig().Getenv("TARGET_BOARD_PLATFORM"),"rk322x")) {
        compile_multilib = "32"
  } else if (strings.EqualFold(ctx.AConfig().Getenv("TARGET_BOARD_PLATFORM"),"rk3326")) {
       if (strings.EqualFold(target_arch,"arm64")) {
                compile_multilib = "both"
        } else {
                compile_multilib = "32"
        }
 } else if (strings.EqualFold(ctx.AConfig().Getenv("TARGET_BOARD_PLATFORM"),"rk3328")) {
        if (strings.EqualFold(target_arch,"arm64")) {
                compile_multilib = "both"
        } else {
                compile_multilib = "32"
        }
 } else if (strings.EqualFold(ctx.AConfig().Getenv("TARGET_BOARD_PLATFORM"),"rk3368")) {
        if (strings.EqualFold(target_arch,"arm64")) {
                compile_multilib = "both"
        } else {
                compile_multilib = "32"
        }
 } else if (strings.EqualFold(ctx.AConfig().Getenv("TARGET_BOARD_PLATFORM"),"rk3399")) {
        if (strings.EqualFold(target_arch,"arm64")) {
                compile_multilib = "both"
        } else {
                compile_multilib = "32"
        }
 } else if (strings.EqualFold(ctx.AConfig().Getenv("TARGET_BOARD_PLATFORM"),"rk3399pro")) {
        if (strings.EqualFold(target_arch,"arm64")) {
                compile_multilib = "both"
        } else {
                compile_multilib = "32"
        }
 }else {
       compile_multilib = "32"
 }
 //fmt.Println("compile_multilib:",compile_multilib)
 return &compile_multilib
}
func globalArm64Defaults(ctx android.LoadHookContext) (Ex_multilibType) {
 var srcs []string
 var multilib Ex_multilibType
 //fmt.Println("TARGET_PRODUCT:",ctx.AConfig().Getenv("TARGET_BOARD_PLATFORM")) 
 if (strings.EqualFold(ctx.AConfig().Getenv("TARGET_BOARD_PLATFORM"),"rk3326")) {
        multilib.Lib32.Srcs = append(srcs,"arm/mpp_dev/libvpu.so")
        multilib.Lib64.Srcs = append(srcs,"arm64/mpp_dev/libvpu.so")
 } else if (strings.EqualFold(ctx.AConfig().Getenv("TARGET_BOARD_PLATFORM"),"rk3328")) {
        multilib.Lib32.Srcs = append(srcs,"arm/mpp_dev/libvpu.so")
        multilib.Lib64.Srcs = append(srcs,"arm64/mpp_dev/libvpu.so")
 }else if (strings.EqualFold(ctx.AConfig().Getenv("TARGET_BOARD_PLATFORM"),"rk3368")) {
        multilib.Lib32.Srcs = append(srcs,"arm/mpp_dev/libvpu.so")
        multilib.Lib64.Srcs = append(srcs,"arm64/mpp_dev/libvpu.so")
 } else if (strings.EqualFold(ctx.AConfig().Getenv("TARGET_BOARD_PLATFORM"),"rk3399")) {
        multilib.Lib32.Srcs = append(srcs,"arm/mpp_dev/libvpu.so")
        multilib.Lib64.Srcs = append(srcs,"arm64/mpp_dev/libvpu.so")
 } else {
        multilib.Lib32.Srcs = append(srcs,"arm/mpp_dev/libvpu.so")
        multilib.Lib64.Srcs = append(srcs,"arm64/mpp_dev/libvpu.so")
 }
        //fmt.Println("multilib.lib32.srcs:",multilib.Lib32.Srcs )
        //fmt.Println("multilib.lib64.srcs:",multilib.Lib64.Srcs)
        return multilib
}
func globalArmDefaults(ctx android.LoadHookContext) ([]string) {
 var srcs []string
 //该打印输出为: TARGET_PRODUCT:rk3328 
 //fmt.Println("TARGET_PRODUCT:",ctx.AConfig().Getenv("TARGET_BOARD_PLATFORM")) 
 if (strings.EqualFold(ctx.AConfig().Getenv("TARGET_BOARD_PLATFORM"),"rk3126c")) {
        srcs = append(srcs,"arm/mpp_dev/libvpu.so")
 } else if (strings.EqualFold(ctx.AConfig().Getenv("TARGET_BOARD_PLATFORM"),"rk322x")) {
        srcs = append(srcs,"arm/mpp_dev/libvpu.so")
 } else if (strings.EqualFold(ctx.AConfig().Getenv("TARGET_BOARD_PLATFORM"),"rk3326")) {
        srcs = append(srcs,"arm/mpp_dev/libvpu.so")
 } else if (strings.EqualFold(ctx.AConfig().Getenv("TARGET_BOARD_PLATFORM"),"rk3328")) {
        srcs = append(srcs,"arm/mpp_dev/libvpu.so")
 }else if (strings.EqualFold(ctx.AConfig().Getenv("TARGET_BOARD_PLATFORM"),"rk3368")) {
        srcs = append(srcs,"arm/mpp_dev/libvpu.so")
 } else if (strings.EqualFold(ctx.AConfig().Getenv("TARGET_BOARD_PLATFORM"),"rk3399")) {
        srcs = append(srcs,"arm/mpp_dev/libvpu.so")
 } else {
        srcs = append(srcs,"arm/mpp_dev/libvpu.so")
 }
 //fmt.Println("srcs:",srcs)
 return srcs
}
