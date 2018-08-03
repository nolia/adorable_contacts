package com.adorablecontacts.ui;

import android.support.v7.app.AppCompatActivity;

import com.adorablecontacts.data.AvatarProvider;
import com.adorablecontacts.data.ContactProvider;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;

import io.reactivex.disposables.CompositeDisposable;

@EActivity
abstract class BaseActivity extends AppCompatActivity {

  @Bean
  protected ContactProvider contactProvider;

  @Bean
  protected AvatarProvider avatarProvider;

  protected CompositeDisposable disposeOnDestroy = new CompositeDisposable();

  @Override
  protected void onDestroy() {
    disposeOnDestroy.dispose();
    super.onDestroy();
  }
}
