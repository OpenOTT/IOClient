package org.openott.ioclient.fragments;

import android.support.v4.app.Fragment;

import org.openott.ioclient.interfaces.IMediaRiteActivity;
import com.futarque.mediarite.IMediaRite;

/**
 * *
 * * Copyright (c) 2015
 * * Futarque A/S - www.futarque.com
 * *
 * * All rights reserved. No part of this software and its documentation
 * * may by used, copied, modified, distributed or sold, in any form or
 * * by any means without the prior written permission of the company.
 * *
 * *
 * * Conformance and Documentation
 * *
 * * All Futarque code MUST comply with the Futarque coding
 * * standard. See MediariteManual.pdf for general documentation.
 * * ------------------------------------------------Futarque-header-end-
 */

public class BaseFragment extends Fragment{
    protected IMediaRite getMediaRite() {
        IMediaRiteActivity act = (IMediaRiteActivity)getActivity();
        return act.getMediaRite();
    }
}
