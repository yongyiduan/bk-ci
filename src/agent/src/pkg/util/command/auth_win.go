//go:build windows
// +build windows

/*
 * Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * BK-CI 蓝鲸持续集成平台 is licensed under the MIT license.
 *
 * A copy of the MIT License is included in this file.
 *
 *
 * Terms of the MIT License:
 * ---------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package command

import (
    "fmt"
    "golang.org/x/sys/windows"
    "unsafe"
)

var procLogonUserW *windows.LazyProc = modadvapi32.NewProc("LogonUserW")

const (
    LOGON32_LOGON_INTERACTIVE = 2
)

const (
    LOGON32_PROVIDER_DEFAULT = 0
)

func windowsLogon(userNameWithDomain, password string) (userToken windows.Token, err error) {
    if returnCode, _, err2 := procLogonUserW.Call(
        uintptr(unsafe.Pointer(windows.StringToUTF16Ptr(userNameWithDomain))),
        0,
        uintptr(unsafe.Pointer(windows.StringToUTF16Ptr(password))),
        LOGON32_LOGON_INTERACTIVE,
        LOGON32_PROVIDER_DEFAULT,
        uintptr(unsafe.Pointer(&userToken)),
    );
        returnCode == 0 {
        err = fmt.Errorf("user %s logon fail: %s", userNameWithDomain, err2)
    }

    return userToken, err
}
