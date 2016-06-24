/*********************************************************************************
 *  DocScan is a Android app for document scanning.
 *
 *  Author:         Fabian Hollaus, Florian Kleber, Markus Diem
 *  Organization:   TU Wien, Computer Vision Lab
 *  Date created:   16. June 2016
 *
 *  This file is part of DocScan.
 *
 *  DocScan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Foobar is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 *********************************************************************************/

#ifndef NO_JNI

#include "DocScanInterface.h"
#include "FocusMeasure.h"
#include "PageSegmentation.h"

#include <android/log.h>
#include <jni.h>

#include <opencv2/core/core.hpp>
#include <opencv2/opencv.hpp>

#include <sstream>
#include <string>
#include <iostream>

JNIEXPORT jobjectArray JNICALL Java_at_ac_tuwien_caa_docscan_NativeWrapper_nativeGetFocusMeasures(JNIEnv * env, jclass cls, jlong src) {


    // call the main function:
    std::vector<dsc::Patch> patches = dsc::FocusEstimation::apply(*((cv::Mat*)src));

    // find the Java Patch class and its constructor:
    jclass patchClass = env->FindClass("at/ac/tuwien/caa/docscan/cv/Patch");
    jmethodID cnstrctr = env->GetMethodID(patchClass, "<init>", "(IIIID)V");
    // "(IIIID)V" -> IIII 4xint D 1xdouble V return void

    // convert the patches vector to a Java array:
    jobjectArray outJNIArray = env->NewObjectArray(patches.size(), patchClass, NULL);

    //jobject patch1 = env->NewObject(patchClass, cnstrctr, 1, 42, 3, 4, 5.4);
    jobject patch;
    for (int i = 0; i < patches.size(); i++) {
        patch = env->NewObject(patchClass, cnstrctr, patches[i].upperLeftX(), patches[i].upperLeftY(),
            patches[i].width(), patches[i].height(), patches[i].fm());
        env->SetObjectArrayElement(outJNIArray, i, patch);
    }


    // set the returned array:


    //setObjectArrayElement(env*, outJNIArray, 0, patch1);

/*
    if (cnstrctr == 0)
        __android_log_write(ANDROID_LOG_INFO, "FocusMeasure", "did not find constructor.");

    else
        __android_log_write(ANDROID_LOG_INFO, "FocusMeasure", "found constructor!");
*/

    //return env->NewObject(c, cnstrctr, 1, 2, 3, 4, 5.4);
    return outJNIArray;

}

#endif // #ifndef NO_JNI