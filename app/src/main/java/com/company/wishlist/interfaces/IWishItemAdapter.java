package com.company.wishlist.interfaces;

/**
 * Created by vladstarikov on 24.02.16.
 */
public interface IWishItemAdapter {

    /**
     * Reserve or unreserve wish
     * @param position - position of wish that will be reserved or unreserved
     */
    void reserveWish(int position);

    /**
     * Remove wish from database
     * @param position position of wish that will be removed
     */
    void removeWish(int position);

    /**
     * Restore all deleted wishes that was backup
     */
    void restoreWish();

}
