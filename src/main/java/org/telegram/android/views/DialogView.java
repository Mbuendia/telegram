package org.telegram.android.views;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.*;
import com.extradea.framework.images.ImageReceiver;
import org.telegram.android.R;
import org.telegram.android.StelsApplication;
import org.telegram.android.core.TypingStates;
import org.telegram.android.core.model.*;
import org.telegram.android.core.model.media.TLLocalAvatarPhoto;
import org.telegram.android.core.model.media.TLLocalFileLocation;
import org.telegram.android.core.model.service.*;
import org.telegram.android.media.StelsImageTask;
import org.telegram.android.ui.EmojiProcessor;
import org.telegram.android.ui.FontController;
import org.telegram.android.ui.Placeholders;
import org.telegram.android.ui.TextUtil;
import org.telegram.i18n.I18nUtil;
import org.telegram.tl.TLObject;

/**
 * Author: Korshakov Stepan
 * Created: 06.08.13 18:40
 */
public class DialogView extends BaseView implements TypingStates.TypingListener {

    // Resources
    private static boolean isLoaded = false;
    private static Paint avatarPaint;
    private static Paint counterPaint;
    private static TextPaint titlePaint;
    private static TextPaint titleHighlightPaint;
    private static TextPaint titleEncryptedPaint;
    private static TextPaint bodyPaint;
    private static TextPaint bodyHighlightPaint;
    private static TextPaint senderPaint;
    private static TextPaint senderHighlightPaint;
    private static TextPaint typingPaint;
    private static TextPaint clockPaint;
    private static TextPaint counterTitlePaint;

    private Drawable statePending;
    private Drawable stateSent;
    private Drawable stateHalfCheck;
    private Drawable stateFailure;
    private Drawable secureIcon;

    // Help data
    private int currentUserUid;

    private StelsApplication application;

    private ImageReceiver avatarReceiver;

    // Data

    private DialogDescription description;

    // PreparedData
    private String title;
    private String body;
    private String senderTitle;
    private String time;
    private String unreadCountText;

    private int state;
    private int unreadCount;

    private Layout bodyLayout;
    private Layout senderLayout;
    private Layout titleLayout;

    private Bitmap empty;
    private Bitmap avatar;

    private boolean isGroup;
    private boolean isEncrypted;
    private boolean isHighlighted;
    private boolean isBodyHighlighted;

    // Typing

    private boolean needNewUpdateTyping;

    private Layout typingLayout;

    private int[] typingUids;
    private boolean userTypes;

    // Layouting

    private boolean isRtl;

    private int layoutAvatarTop;
    private int layoutAvatarLeft;
    private int layoutTimeTop;
    private int layoutTimeLeft;

    private int layoutTitleTop;
    private int layoutTitleLeft;
    private int layoutTitleWidth;

    private int layoutEncryptedTop;
    private int layoutEncryptedLeft;

    private int layoutGroupSenderTop;
    private int layoutGroupSenderLeft;

    private int layoutGroupContentTop;

    private int layoutStateTop;
    private int layoutStateLeft;
    private int layoutStateLeftDouble;

    private int layoutMainWidth;
    private int layoutMainLeft;
    private int layoutMainTop;

    private int layoutMarkWidth;
    private int layoutMarkLeft;
    private int layoutMarkTop;
    private int layoutMarkBottom;
    private int layoutMarkTextLeft;
    private int layoutMarkTextTop;
    private int layoutMarkRadius;
    private RectF layoutMarkRect;

    public DialogView(Context context) {
        super(context);

        this.application = (StelsApplication) context.getApplicationContext();
        this.currentUserUid = application.getCurrentUid();
        this.isRtl = application.isRTL();
        this.avatarReceiver = new ImageReceiver() {
            @Override
            public void onImageLoaded(Bitmap result) {
                avatar = result;
                postInvalidate();
            }

            @Override
            public void onImageLoadFailure() {
                avatar = null;
                postInvalidate();
            }

            @Override
            public void onNoImage() {
                avatar = null;
                postInvalidate();
            }
        };
        this.avatarReceiver.register(application.getImageController());

        application.getTypingStates().registerListener(this);

        if (!isLoaded) {
            avatarPaint = new Paint();

            titlePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
            titlePaint.setColor(0xff010101);
            titlePaint.setTextSize(getSp(17.5f));

            titleHighlightPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
            titleHighlightPaint.setColor(0xff006FC8);
            titleHighlightPaint.setTextSize(getSp(17.5f));

            titleEncryptedPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
            titleEncryptedPaint.setColor(0xff68b741);
            titleEncryptedPaint.setTextSize(getSp(17.5f));

            clockPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
            clockPaint.setColor(0xff006FC8);
            clockPaint.setTextSize(getSp(14));

            bodyPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
            bodyPaint.setColor(0xff808080);
            bodyPaint.setTextSize(getSp(15.5f));
            bodyPaint.setTypeface(FontController.loadTypeface(context, "light"));

            bodyHighlightPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
            bodyHighlightPaint.setColor(0xff006FC8);
            bodyHighlightPaint.setTextSize(getSp(15.5f));
            bodyHighlightPaint.setTypeface(FontController.loadTypeface(context, "light"));

            senderPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
            senderPaint.setColor(0xff808080);
            senderPaint.setTextSize(getSp(16f));

            senderHighlightPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
            senderHighlightPaint.setColor(0xff006FC8);
            senderHighlightPaint.setTextSize(getSp(16f));

            typingPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
            typingPaint.setColor(0xff006FC8);
            typingPaint.setTextSize(getSp(16f));

            counterTitlePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
            counterTitlePaint.setColor(0xffffffff);
            counterTitlePaint.setTextSize(getSp(15.5f));

            counterPaint = new Paint();
            counterPaint.setColor(0xff4595D6);
            isLoaded = true;
        }

        statePending = getResources().getDrawable(R.drawable.st_bubble_ic_clock);
        stateSent = getResources().getDrawable(R.drawable.st_dialogs_check);
        stateHalfCheck = getResources().getDrawable(R.drawable.st_dialogs_halfcheck);
        stateFailure = getResources().getDrawable(R.drawable.st_dialogs_warning);
        secureIcon = getResources().getDrawable(R.drawable.st_ic_lock_green);
    }

    public void setDescription(DialogDescription description) {
        this.description = description;
        this.time = org.telegram.android.ui.TextUtil.formatDate(description.getDate(), getContext());
        this.state = description.getMessageState();
        this.unreadCount = description.getUnreadCount();

        if (description.getPeerType() == PeerType.PEER_CHAT) {
            this.typingUids = application.getTypingStates().getChatTypes(description.getPeerId());
        } else {
            this.userTypes = application.getTypingStates().isUserTyping(description.getPeerId());
        }

        prepareData();

        if (description.getPeerType() == PeerType.PEER_USER) {
            if (description.getPeerId() == 333000) {
                empty = ((BitmapDrawable) getResources().getDrawable(R.drawable.st_support_avatar)).getBitmap();
            } else {
                empty = ((BitmapDrawable) getResources().getDrawable(Placeholders.getUserPlaceholder(description.getPeerId()))).getBitmap();
            }
        } else {
            empty = ((BitmapDrawable) getResources().getDrawable(Placeholders.getGroupPlaceholder(description.getPeerId()))).getBitmap();
        }

        if (description.getPhoto() instanceof TLLocalAvatarPhoto) {
            TLLocalAvatarPhoto avatarPhoto = (TLLocalAvatarPhoto) description.getPhoto();
            if (avatarPhoto.getPreviewLocation() instanceof TLLocalFileLocation) {
                StelsImageTask task = new StelsImageTask((TLLocalFileLocation) avatarPhoto.getPreviewLocation());
                task.setMaxHeight(getPx(64));
                task.setMaxWidth(getPx(64));
                task.setFillRect(true);
                avatarReceiver.receiveImage(task);
                avatar = avatarReceiver.getResult();
            } else {
                avatarReceiver.receiveImage(null);
            }
        } else {
            avatarReceiver.receiveImage(null);
        }

        if (getMeasuredHeight() != 0 || getMeasuredWidth() != 0) {
            buildLayout();
        } else {
            requestLayout();
        }

        needNewUpdateTyping = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        avatarReceiver.onRemovedFromParent();
        avatar = null;
        postInvalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        avatarReceiver.onAddedToParent();
        avatar = avatarReceiver.getResult();
        postInvalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), getPx(80));
    }

    private void prepareData() {
        // Title and highlight
        if (description.getPeerType() == PeerType.PEER_USER) {
            if (description.getPeerId() == 333000) {
                this.title = "Telegram";
                this.isHighlighted = false;
            } else {
                User user = application.getEngine().getUserRuntime(description.getPeerId());
                this.isHighlighted = user.getLinkType() == LinkType.FOREIGN;
                if (user.getLinkType() == LinkType.REQUEST) {
                    this.title = TextUtil.formatPhone(user.getPhone());
                } else {
                    this.title = user.getDisplayName();
                }
            }
            this.isGroup = false;
            this.isEncrypted = false;
        } else if (description.getPeerType() == PeerType.PEER_CHAT) {
            this.title = description.getTitle();
            this.isHighlighted = false;
            this.isGroup = true;
            this.isEncrypted = false;
        } else if (description.getPeerType() == PeerType.PEER_USER_ENCRYPTED) {
            EncryptedChat chat = application.getEngine().getEncryptedChat(description.getPeerId());
            User user = application.getEngine().getUserRuntime(chat.getUserId());
            if (user.getLinkType() == LinkType.REQUEST) {
                this.title = TextUtil.formatPhone(user.getPhone());
            } else {
                this.title = user.getDisplayName();
            }
            this.isHighlighted = false;
            this.isGroup = false;
            this.isEncrypted = true;
        }

        // Body
        isBodyHighlighted = description.getContentType() != ContentType.MESSAGE_TEXT;

        if (isGroup) {
            if (description.getSenderId() == currentUserUid) {
                senderTitle = getResources().getString(R.string.st_dialog_you);
            } else {
                senderTitle = description.getSenderTitle();
            }
        }

        if (description.getRawContentType() == ContentType.MESSAGE_TEXT) {
            String rawMessage = description.getMessage();
            if (rawMessage.length() > 50) {
                rawMessage = rawMessage.substring(0, 50);
            }
            String[] rows = rawMessage.split("\n", 3);
            if (rows.length == 3) {
                rawMessage = rows[0] + "\n" + rows[1];
            }
            body = rawMessage;
        } else if (description.getRawContentType() == ContentType.MESSAGE_SYSTEM) {
            TLObject object = description.getExtras();
            if (object != null && object instanceof TLAbsLocalAction) {
                boolean isMyself = description.getSenderId() == currentUserUid;
                if (object instanceof TLLocalActionChatCreate) {
                    body = getResources().getString(isMyself ? R.string.st_dialog_created_group_you : R.string.st_dialog_created_group);
                } else if (object instanceof TLLocalActionChatDeleteUser) {
                    int uid = ((TLLocalActionChatDeleteUser) object).getUserId();
                    if (uid == description.getSenderId()) {
                        body = getResources().getString(isMyself ? R.string.st_dialog_left_user_you : R.string.st_dialog_left_user);
                    } else {
                        if (uid == currentUserUid) {
                            body = getResources().getString(R.string.st_dialog_kicked_user_of_you).replace("{0}", getResources().getString(R.string.st_dialog_you_r));
                        } else {
                            User usr = application.getEngine().getUserRuntime(uid);
                            body = getResources().getString(isMyself ? R.string.st_dialog_kicked_user_you : R.string.st_dialog_kicked_user).replace("{0}", usr.getDisplayName());
                        }
                    }
                } else if (object instanceof TLLocalActionChatAddUser) {
                    int uid = ((TLLocalActionChatAddUser) object).getUserId();
                    if (uid == description.getSenderId()) {
                        body = getResources().getString(isMyself ? R.string.st_dialog_enter_user_you : R.string.st_dialog_enter_user);
                    } else {
                        if (uid == currentUserUid) {
                            body = getResources().getString(R.string.st_dialog_added_user_of_you).replace("{0}", getResources().getString(R.string.st_dialog_you_r));
                        } else {
                            User usr = application.getEngine().getUserRuntime(uid);
                            body = getResources().getString(isMyself ? R.string.st_dialog_added_user_you : R.string.st_dialog_added_user).replace("{0}", usr.getDisplayName());
                        }
                    }
                } else if (object instanceof TLLocalActionChatDeletePhoto) {
                    body = getResources().getString(isMyself ? R.string.st_dialog_removed_photo_you : R.string.st_dialog_removed_photo);
                } else if (object instanceof TLLocalActionChatEditPhoto) {
                    body = getResources().getString(isMyself ? R.string.st_dialog_changed_photo_you : R.string.st_dialog_changed_photo);
                } else if (object instanceof TLLocalActionChatEditTitle) {
                    body = getResources().getString(isMyself ? R.string.st_dialog_changed_name_you : R.string.st_dialog_changed_name);
                } else if (object instanceof TLLocalActionUserRegistered) {
                    body = getResources().getString(R.string.st_dialog_user_joined_app);
                } else if (object instanceof TLLocalActionUserEditPhoto) {
                    body = getResources().getString(R.string.st_dialog_user_add_avatar);
                } else if (object instanceof TLLocalActionEncryptedTtl) {
                    TLLocalActionEncryptedTtl ttl = (TLLocalActionEncryptedTtl) object;
                    if (description.getSenderId() == application.getCurrentUid()) {
                        if (ttl.getTtlSeconds() > 0) {
                            body = getResources().getString(R.string.st_dialog_encrypted_switched_you).replace(
                                    "{time}", TextUtil.formatHumanReadableDuration(ttl.getTtlSeconds()));
                        } else {
                            body = getResources().getString(R.string.st_dialog_encrypted_switched_off_you);
                        }
                    } else {
                        if (ttl.getTtlSeconds() > 0) {
                            body = getResources().getString(R.string.st_dialog_encrypted_switched).replace(
                                    "{time}", TextUtil.formatHumanReadableDuration(ttl.getTtlSeconds()));
                        } else {
                            body = getResources().getString(R.string.st_dialog_encrypted_switched_off);
                        }
                    }
                } else if (object instanceof TLLocalActionEncryptedCancelled) {
                    body = getResources().getString(R.string.st_dialog_encrypted_cancelled);
                } else if (object instanceof TLLocalActionEncryptedRequested) {
                    body = getResources().getString(R.string.st_dialog_encrypted_requested);
                } else if (object instanceof TLLocalActionEncryptedWaiting) {
                    EncryptedChat encryptedChat = application.getEngine().getEncryptedChat(description.getPeerId());
                    User u = application.getEngine().getUser(encryptedChat.getUserId());
                    body = getResources().getString(R.string.st_dialog_encrypted_waiting)
                            .replace("{name}", u.getFirstName());
                } else if (object instanceof TLLocalActionEncryptedCreated) {
                    body = getResources().getString(R.string.st_dialog_encrypted_created);
                } else if (object instanceof TLLocalActionEncryptedMessageDestructed) {
                    body = getResources().getString(R.string.st_dialog_encrypted_selfdestructed);
                } else {
                    body = getResources().getString(R.string.st_dialog_system);
                }
            } else {
                body = getResources().getString(R.string.st_dialog_system);
            }
        } else {
            switch (description.getRawContentType()) {
                case ContentType.MESSAGE_VIDEO:
                    body = getResources().getString(R.string.st_dialog_video);
                    break;
                case ContentType.MESSAGE_GEO:
                    body = getResources().getString(R.string.st_dialog_geo);
                    break;
                case ContentType.MESSAGE_PHOTO:
                    body = getResources().getString(R.string.st_dialog_photo);
                    break;
                case ContentType.MESSAGE_CONTACT:
                    body = getResources().getString(R.string.st_dialog_contact);
                    break;
                default:
                    body = getResources().getString(R.string.st_dialog_unknown);
                    break;
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            buildLayout();
        }
    }

    private void buildLayout() {
        layoutAvatarTop = getPx(8);
        if (isRtl) {
            layoutAvatarLeft = getMeasuredWidth() - getPx(8 + 64);
        } else {
            layoutAvatarLeft = getPx(8);
        }

        int timeWidth = (int) clockPaint.measureText(time);

        layoutTimeTop = getPx(24);
        if (isRtl) {
            layoutTimeLeft = getPx(8);
        } else {
            layoutTimeLeft = getMeasuredWidth() - getPx(8) - timeWidth;
        }

        layoutStateTop = getPx(13);

        if (isRtl) {
            layoutStateLeft = getPx(8) + timeWidth + getPx(7);
            layoutStateLeftDouble = getPx(8) + timeWidth + getPx(2);
        } else {
            layoutStateLeft = getMeasuredWidth() - getPx(8) - timeWidth - getPx(16);
            layoutStateLeftDouble = getMeasuredWidth() - getPx(8) - timeWidth - getPx(21);
        }

        layoutMarkTop = getPx(40);
        layoutMarkTextTop = getPx(18);
        layoutMarkBottom = getPx(62);
        layoutMarkRadius = getPx(2);
        layoutMarkTextTop = getPx(58);
        if (description.isFailure() ||
                (state == MessageState.FAILURE && description.getSenderId() == currentUserUid)) {
            layoutMarkWidth = getPx(30);
            if (isRtl) {
                layoutMarkLeft = getPx(8); // getMeasuredWidth() - layoutMarkWidth - getPx(80);
            } else {
                layoutMarkLeft = getMeasuredWidth() - layoutMarkWidth - getPx(8);
            }
        } else {
            if (unreadCount > 0) {
                if (unreadCount >= 1000) {
                    unreadCountText = I18nUtil.getInstance().correctFormatNumber(unreadCount / 1000) + "K";
                } else {
                    unreadCountText = I18nUtil.getInstance().correctFormatNumber(unreadCount);
                }
                int width = (int) counterTitlePaint.measureText(unreadCountText);
                Rect r = new Rect();
                counterTitlePaint.getTextBounds(unreadCountText, 0, unreadCountText.length(), r);
                layoutMarkTextTop = layoutMarkTop + (layoutMarkBottom - layoutMarkTop + r.top) / 2 - r.top;
                if (width < getPx(22 - 14)) {
                    layoutMarkWidth = getPx(22);
                } else {
                    layoutMarkWidth = getPx(14) + width;
                }
                layoutMarkTextLeft = (layoutMarkWidth - width) / 2;

                if (isRtl) {
                    layoutMarkLeft = getPx(8); //getMeasuredWidth() - layoutMarkWidth - getPx(80);
                } else {
                    layoutMarkLeft = getMeasuredWidth() - layoutMarkWidth - getPx(8);
                }
            } else {
                layoutMarkLeft = 0;
                layoutMarkWidth = 0;
            }
        }
        layoutMarkRect = new RectF(layoutMarkLeft, layoutMarkTop, layoutMarkLeft + layoutMarkWidth, layoutMarkBottom);

        layoutTitleTop = getPx(8);
        if (isEncrypted) {
            layoutEncryptedTop = getPx(10);
            if (isRtl) {
                if (description.getSenderId() == application.getCurrentUid()) {
                    layoutTitleLeft = timeWidth + getPx(16) + getPx(16);
                } else {
                    layoutTitleLeft = timeWidth + getPx(12);
                }
                layoutTitleWidth = getMeasuredWidth() - layoutTitleLeft - getPx(80) - getPx(14) - getPx(6);
                layoutEncryptedLeft = getMeasuredWidth() - getPx(80) - getPx(12);
            } else {
                layoutTitleLeft = getPx(80) + getPx(18);
                if (description.getSenderId() == application.getCurrentUid()) {
                    layoutTitleWidth = getMeasuredWidth() - layoutTitleLeft - timeWidth - getPx(16);
                } else {
                    layoutTitleWidth = getMeasuredWidth() - layoutTitleLeft - timeWidth - getPx(4);
                }

                layoutEncryptedLeft = getPx(84);
            }
        } else {
            if (isRtl) {
                if (description.getSenderId() == application.getCurrentUid()) {
                    layoutTitleLeft = timeWidth + getPx(16) + getPx(16);
                } else {
                    layoutTitleLeft = timeWidth + getPx(12);
                }
                layoutTitleWidth = getMeasuredWidth() - layoutTitleLeft - getPx(80);
            } else {
                layoutTitleLeft = getPx(80);
                if (description.getSenderId() == application.getCurrentUid()) {
                    layoutTitleWidth = getMeasuredWidth() - layoutTitleLeft - timeWidth - getPx(24) - getPx(16);
                } else {
                    layoutTitleWidth = getMeasuredWidth() - layoutTitleLeft - timeWidth - getPx(16);
                }
            }
        }

        if (isGroup) {
            layoutGroupSenderTop = getPx(46);
            if (isRtl) {
                int senderWidth = (int) senderPaint.measureText(senderTitle);
                layoutGroupSenderLeft = getMeasuredWidth() - getPx(80) - senderWidth;
            } else {
                layoutGroupSenderLeft = getPx(80);
            }
        }

        layoutMainTop = getPx(32);
        layoutMainWidth = getMeasuredWidth() - getPx(80 + 8);
        if (isRtl) {
            layoutMainLeft = getMeasuredWidth() - layoutMainWidth - getPx(80);
            if (layoutMarkWidth != 0) {
                layoutMainLeft += layoutMarkWidth + getPx(8);
                layoutMainWidth -= layoutMarkWidth + getPx(8);
            }
        } else {
            layoutMainLeft = getPx(80);
            if (layoutMarkWidth != 0) {
                layoutMainWidth -= layoutMarkWidth + getPx(8);
            }
        }

        layoutGroupContentTop = getPx(66 - 14);

        // Building text layouts
        // Body
        if (EmojiProcessor.containsEmoji(body)) {
            if (isGroup) {
                String str;
                if (application.isRTL()) {
                    str = body.replace("\n", " ");
                } else {
                    str = body.replace("\n", " ");
                }
                CharSequence sequence = application.getEmojiProcessor().processEmojiCutMutable(str, EmojiProcessor.CONFIGURATION_DIALOGS);
                sequence = TextUtils.ellipsize(sequence, bodyPaint, layoutMainWidth, TextUtils.TruncateAt.END);
                bodyLayout = new StaticLayout(sequence, isBodyHighlighted ? bodyHighlightPaint : bodyPaint, layoutMainWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            } else {
                String str;
                if (application.isRTL()) {
                    str = body;
                } else {
                    str = body;
                }
                CharSequence sequence = application.getEmojiProcessor().processEmojiCutMutable(str, EmojiProcessor.CONFIGURATION_DIALOGS);
                bodyLayout = new StaticLayout(sequence, isBodyHighlighted ? bodyHighlightPaint : bodyPaint, layoutMainWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                if (bodyLayout.getLineCount() > 2) {
                    int start = bodyLayout.getLineStart(1);
                    String rawFirstLine = str.substring(0, start);
                    String rawLastLine = str.substring(start, bodyLayout.getLineEnd(1));
                    rawLastLine = TextUtils.ellipsize(application.getEmojiProcessor().processEmojiMutable(rawLastLine, EmojiProcessor.CONFIGURATION_DIALOGS), bodyPaint, layoutMainWidth, TextUtils.TruncateAt.END).toString();
                    sequence = application.getEmojiProcessor().processEmojiCompatMutable(rawFirstLine + rawLastLine, EmojiProcessor.CONFIGURATION_DIALOGS);
                    bodyLayout = new StaticLayout(sequence, isBodyHighlighted ? bodyHighlightPaint : bodyPaint, layoutMainWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                }
            }
        } else {
            if (isGroup) {
                CharSequence sequence = TextUtils.ellipsize(body.replace("\n", " "), bodyPaint, layoutMainWidth, TextUtils.TruncateAt.END);
                bodyLayout = new StaticLayout(sequence, isBodyHighlighted ? bodyHighlightPaint : bodyPaint, layoutMainWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            } else {
                bodyLayout = new StaticLayout(body, isBodyHighlighted ? bodyHighlightPaint : bodyPaint, layoutMainWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                if (bodyLayout.getLineCount() > 2) {
                    int start = bodyLayout.getLineStart(1);
                    String rawFirstLine = body.substring(0, start);
                    String rawLastLine = body.substring(start, bodyLayout.getLineEnd(1));
                    rawLastLine = TextUtils.ellipsize(application.getEmojiProcessor().processEmojiMutable(rawFirstLine + rawLastLine, EmojiProcessor.CONFIGURATION_DIALOGS), bodyPaint, layoutMainWidth, TextUtils.TruncateAt.END).toString();
                    bodyLayout = new StaticLayout(rawLastLine, isBodyHighlighted ? bodyHighlightPaint : bodyPaint, layoutMainWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                }
            }
        }

        // Title
        {
            String wTitle = title.replace("\n", " ");
            if (wTitle.length() > 100) {
                wTitle = wTitle.substring(100) + "...";
            }
            TextPaint paint = isEncrypted ? titleEncryptedPaint : (isHighlighted ? titleHighlightPaint : titlePaint);
            CharSequence sequence = TextUtils.ellipsize(wTitle, paint, layoutTitleWidth, TextUtils.TruncateAt.END);
            titleLayout = new StaticLayout(sequence, paint, layoutTitleWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        }

        // Group sender
        if (isGroup) {
            String wName = senderTitle.replace("\n", " ");
            if (wName.length() > 100) {
                wName = wName.substring(100) + "...";
            }
            TextPaint spaint = description.getSenderId() == currentUserUid ? senderPaint : senderHighlightPaint;
            CharSequence ssequence = TextUtils.ellipsize(wName, spaint, layoutMainWidth, TextUtils.TruncateAt.END);
            senderLayout = new StaticLayout(ssequence, spaint, layoutMainWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        }
    }

    private void bound(Drawable src, int x, int y) {
        src.setBounds(x, y, x + src.getIntrinsicWidth(), y + src.getIntrinsicHeight());
    }

    private void updateTyping() {
        needNewUpdateTyping = false;
        if (description.getPeerType() == PeerType.PEER_USER || description.getPeerType() == PeerType.PEER_USER_ENCRYPTED) {
            if (userTypes) {
                typingLayout = new StaticLayout(getResources().getString(R.string.lang_common_typing), bodyHighlightPaint, layoutMainWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            } else {
                typingLayout = null;
            }
        } else {
            if (typingUids != null && typingUids.length != 0) {
                String[] names = new String[typingUids.length];
                for (int i = 0; i < names.length; i++) {
                    names[i] = application.getEngine().getUserRuntime(typingUids[i]).getFirstName();
                }
                typingLayout = new StaticLayout(TextUtil.formatTyping(names), bodyHighlightPaint, layoutMainWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            } else {
                typingLayout = null;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (description == null) {
            return;
        }

        if (needNewUpdateTyping) {
            updateTyping();
        }

        if (isEncrypted) {
            bounds(secureIcon, layoutEncryptedLeft, layoutEncryptedTop);
            secureIcon.draw(canvas);
        }

        canvas.save();
        canvas.translate(layoutTitleLeft, layoutTitleTop);
        titleLayout.draw(canvas);
        canvas.restore();

        if (state != MessageState.FAILURE && description.getSenderId() == currentUserUid) {
            switch (state) {
                default:
                case MessageState.PENDING:
                    bound(statePending, layoutStateLeft, layoutStateTop);
                    statePending.draw(canvas);
                    break;
                case MessageState.SENT:
                    bound(stateSent, layoutStateLeft, layoutStateTop);
                    stateSent.draw(canvas);
                    break;
                case MessageState.READED:
                    bound(stateSent, layoutStateLeftDouble, layoutStateTop);
                    stateSent.draw(canvas);
                    bound(stateHalfCheck, layoutStateLeft, layoutStateTop);
                    stateHalfCheck.draw(canvas);
                    break;
            }
        }

        canvas.drawText(time, layoutTimeLeft, layoutTimeTop, clockPaint);

        if (typingLayout != null) {
            canvas.save();
            canvas.translate(layoutMainLeft, layoutMainTop);
            typingLayout.draw(canvas);
            canvas.restore();
        } else {
            if (isGroup) {
                canvas.save();
                canvas.translate(layoutMainLeft, layoutMainTop);
                senderLayout.draw(canvas);
                canvas.restore();

                canvas.save();
                canvas.translate(layoutMainLeft, layoutGroupContentTop);
                bodyLayout.draw(canvas);
                canvas.restore();
            } else {
                canvas.save();
                canvas.translate(layoutMainLeft, layoutMainTop);
                bodyLayout.draw(canvas);
                canvas.restore();
            }
        }

        if (avatar != null) {
            canvas.drawBitmap(avatar, layoutAvatarLeft, layoutAvatarTop, avatarPaint);
        } else {
            canvas.drawBitmap(empty, layoutAvatarLeft, layoutAvatarTop, avatarPaint);
        }

        if (description.isFailure() ||
                (state == MessageState.FAILURE && description.getSenderId() == currentUserUid)) {
            bound(stateFailure, layoutMarkLeft, layoutMarkTop);
            stateFailure.draw(canvas);
        } else if (unreadCount > 0) {
            canvas.drawRoundRect(layoutMarkRect, layoutMarkRadius, layoutMarkRadius, counterPaint);
            canvas.drawText(unreadCountText, layoutMarkLeft + layoutMarkTextLeft, layoutMarkTextTop, counterTitlePaint);
        }
    }

    @Override
    public void onChatTypingChanged(int chatId, int[] uids) {
        if (description != null) {
            if (description.getPeerType() == PeerType.PEER_CHAT && description.getPeerId() == chatId) {
                this.typingUids = uids;
                this.needNewUpdateTyping = true;
                this.invalidate();
            }
        }
    }

    @Override
    public void onUserTypingChanged(int uid, boolean types) {
        if (description != null) {
            if (description.getPeerType() == PeerType.PEER_USER & description.getPeerId() == uid) {
                this.userTypes = types;
                this.needNewUpdateTyping = true;
                this.invalidate();
            }
        }
    }

    @Override
    public void onEncryptedTypingChanged(int chatId, boolean types) {
        if (description != null) {
            if (description.getPeerType() == PeerType.PEER_USER_ENCRYPTED & description.getPeerId() == chatId) {
                this.userTypes = types;
                this.needNewUpdateTyping = true;
                this.invalidate();
            }
        }
    }
}