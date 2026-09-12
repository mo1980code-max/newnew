package org.Allah_Clock_Live_Wallpaper.ads;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdImage;
import com.google.android.gms.ads.nativead.NativeAdView;

import org.Allah_Clock_Live_Wallpaper.R;

/**
 * Binds a {@link NativeAd} into {@code item_native_ad.xml}.
 *
 * <p>Every optional asset is hidden when the ad network did not send it, and
 * {@link NativeAdView#setNativeAd(NativeAd)} is called last so the SDK can register the
 * clickable assets and drop in the AdChoices icon.</p>
 */
final class NativeAdViewHolder extends RecyclerView.ViewHolder {

    private final NativeAdView adView;
    private final MediaView mediaView;
    private final ImageView iconView;
    private final TextView headlineView;
    private final TextView bodyView;
    private final TextView advertiserView;
    private final TextView priceView;
    private final TextView storeView;
    private final Button callToActionView;
    private final RatingBar starsView;

    NativeAdViewHolder(@NonNull View itemView) {
        super(itemView);
        adView = (NativeAdView) itemView;
        mediaView = itemView.findViewById(R.id.ad_media);
        iconView = itemView.findViewById(R.id.ad_icon);
        headlineView = itemView.findViewById(R.id.ad_headline);
        bodyView = itemView.findViewById(R.id.ad_body);
        advertiserView = itemView.findViewById(R.id.ad_advertiser);
        priceView = itemView.findViewById(R.id.ad_price);
        storeView = itemView.findViewById(R.id.ad_store);
        callToActionView = itemView.findViewById(R.id.ad_call_to_action);
        starsView = itemView.findViewById(R.id.ad_stars);
    }

    void bind(@Nullable NativeAd nativeAd) {
        if (nativeAd == null) {
            itemView.setVisibility(View.GONE);
            itemView.setLayoutParams(new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT, 0));
            return;
        }
        itemView.setVisibility(View.VISIBLE);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT));

        adView.setMediaView(mediaView);
        adView.setHeadlineView(headlineView);
        adView.setBodyView(bodyView);
        adView.setCallToActionView(callToActionView);
        adView.setIconView(iconView);
        adView.setStarRatingView(starsView);
        adView.setAdvertiserView(advertiserView);
        adView.setPriceView(priceView);
        adView.setStoreView(storeView);

        setText(headlineView, nativeAd.getHeadline());
        setText(bodyView, nativeAd.getBody());
        setText(advertiserView, nativeAd.getAdvertiser());
        setText(priceView, nativeAd.getPrice());
        setText(storeView, nativeAd.getStore());
        setText(callToActionView, nativeAd.getCallToAction());

        Double rating = nativeAd.getStarRating();
        if (rating != null && rating > 0.0d) {
            starsView.setRating(rating.floatValue());
            starsView.setVisibility(View.VISIBLE);
        } else {
            starsView.setVisibility(View.GONE);
        }

        NativeAdImage icon = nativeAd.getIcon();
        if (icon == null) {
            iconView.setVisibility(View.GONE);
        } else if (icon.getDrawable() != null) {
            iconView.setVisibility(View.VISIBLE);
            iconView.setImageDrawable(icon.getDrawable());
        } else if (icon.getUri() != null) {
            iconView.setVisibility(View.VISIBLE);
            Glide.with(iconView.getContext()).load(icon.getUri()).into(iconView);
        } else {
            iconView.setVisibility(View.GONE);
        }

        // Must be last: hands the ad over to the SDK for click handling and attribution.
        adView.setNativeAd(nativeAd);
    }

    private static void setText(@NonNull TextView view, @Nullable CharSequence value) {
        if (value == null || value.length() == 0) {
            view.setVisibility(View.GONE);
            return;
        }
        view.setVisibility(View.VISIBLE);
        view.setText(value);
    }
}
