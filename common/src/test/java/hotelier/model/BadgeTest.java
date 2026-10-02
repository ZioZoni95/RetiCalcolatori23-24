package hotelier.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BadgeTest {

    @Test
    void thresholds() {
        assertEquals(Badge.REVIEWER, Badge.forReviewCount(0));
        assertEquals(Badge.REVIEWER, Badge.forReviewCount(1));
        assertEquals(Badge.EXPERT_REVIEWER, Badge.forReviewCount(2));
        assertEquals(Badge.CONTRIBUTOR, Badge.forReviewCount(3));
        assertEquals(Badge.EXPERT_CONTRIBUTOR, Badge.forReviewCount(4));
        assertEquals(Badge.SUPER_CONTRIBUTOR, Badge.forReviewCount(5));
        assertEquals(Badge.SUPER_CONTRIBUTOR, Badge.forReviewCount(50));
    }

    @Test
    void userBadgeFollowsReviewCount() {
        User user = User.create("mario", "hash");
        for (int i = 0; i < 3; i++) {
            user = user.withNewReview();
        }
        assertEquals(3, user.reviewCount());
        assertEquals(Badge.CONTRIBUTOR, user.badgeLevel());
    }
}
