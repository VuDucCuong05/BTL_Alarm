package com.simplemobiletools.clock.activities
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.activities.FAQActivity
import android.content.Intent
import android.os.Bundle
import android.view.Menu

import com.simplemobiletools.clock.R

import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.FAQItem
import kotlinx.android.synthetic.main.activity_about.*
import java.util.*

class AboutActivity : BaseSimpleActivity() {
    private var appName = ""
    private var linkColor = 0

    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        appName = intent.getStringExtra(APP_NAME) ?: ""
        linkColor = getAdjustedPrimaryColor()
    }

    override fun onResume() {
        super.onResume()
        updateTextColors(about_holder)
        setupAboutFork()
        setupWebsite()
        setupEmail()
        setupFAQ()
        setupCopyright()
    }

    private fun setupAboutFork() {
        val websiteText = String.format(getString(R.string.two_string_placeholder), getString(com.simplemobiletools.clock.R.string.website_label), getString(com.simplemobiletools.clock.R.string.my_website))
        about_fork.text = websiteText
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        updateMenuItemColors(menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun setupWebsite() {
//        val websiteText = String.format(getString(R.string.two_string_placeholder), getString(R.string.website_label), getString(R.string.my_website))
//        about_website.text = websiteText
    }

    private fun setupEmail() {
        val websiteText = String.format(getString(R.string.two_string_placeholder), getString(R.string.my_email_coxtor), getString(R.string.my_email_coxtor))
        about_email.text = websiteText
    }

    private fun setupFAQ() {
        val faqItems = intent.getSerializableExtra(APP_FAQ) as ArrayList<FAQItem>
        about_faq_label.beVisibleIf(faqItems.isNotEmpty())
        about_faq_label.setOnClickListener {
            openFAQ(faqItems)
        }

        about_faq.beVisibleIf(faqItems.isNotEmpty())
        about_faq.setOnClickListener {
            openFAQ(faqItems)
        }

        about_faq.setTextColor(linkColor)
        about_faq.underlineText()
    }


    private fun openFAQ(faqItems: ArrayList<FAQItem>) {
        Intent(applicationContext, FAQActivity::class.java).apply {
            putExtra(APP_ICON_IDS, getAppIconIDs())
            putExtra(APP_LAUNCHER_NAME, getAppLauncherName())
            putExtra(APP_FAQ, faqItems)
            startActivity(this)
        }
    }

    private fun setupCopyright() {
        var versionName = intent.getStringExtra(APP_VERSION_NAME) ?: ""

        val year = Calendar.getInstance().get(Calendar.YEAR)
        about_copyright.text = String.format(getString(R.string.copyright), versionName, year)
    }
}
