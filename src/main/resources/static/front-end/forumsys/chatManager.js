// front-end/forumsys/chatManager.js

// --- 全域變數，管理連線狀態 ---
let stompClient = null;
let subscription = null;
let currentForumId = null;
let currentUsername = null;
let currentMemberId = null; // 【新增】 用來存放當前使用者的 ID

// --- DOM 元素快取 ---
let messageInput, chatForm, messagesArea, chatStatus;

/**
 * 【核心函式】: 連接到指定 forumId 的 WebSocket
 * @param {string} forumId - 討論區 ID
 * @param {string} username - 當前登入的使用者名稱
 * @param {number} memberId - 【新增】當前登入的使用者 ID
 */
// 【修改】 connect 函式簽名，增加 memberId 參數
function connect(forumId, username, memberId) {
    // DOM 元素在 connect 時才獲取
    messageInput = document.getElementById('chat-message-input');
    chatForm = document.getElementById('chat-form');
    messagesArea = document.getElementById('chat-messages');
    chatStatus = document.getElementById('chat-status');

    // 儲存當前狀態
    currentForumId = forumId;
    currentUsername = username;
    currentMemberId = memberId; // 【新增】 儲存使用者 ID

    if (username && stompClient === null) {
        updateChatStatus('connecting');
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, onConnected, onError);
    }

    if (chatForm) {
        chatForm.removeEventListener('submit', sendMessage); // 【優化】先移除舊的監聽器，避免重複綁定
        chatForm.addEventListener('submit', sendMessage);
    }
}

/**
 * 連線成功後的回呼函式
 */
function onConnected() {
    updateChatStatus('connected');
    messageInput.disabled = false;
    chatForm.querySelector('button').disabled = false;

    subscription = stompClient.subscribe(`/topic/chat/${currentForumId}`, onMessageReceived);

    // 【修改】addUser 時，要把 memberId 一起傳過去
    stompClient.send(`/api/chat/${currentForumId}/addUser`,
        {},
        JSON.stringify({
            sender: currentUsername,
            type: 'JOIN',
            memberId: currentMemberId // 【新增】
        })
    );
}

/**
 * 連線失敗後的回呼函式
 */
function onError(error) {
    updateChatStatus('error');
    console.error('WebSocket 連接失敗:', error);
}

/**
 * 【核心修改】收到訊息時的回呼函式
 * @param {object} payload - 伺服器傳來的訊息封包
 */
function onMessageReceived(payload) {
    const message = JSON.parse(payload.body); // message 格式: {sender, content, type, memberId}
    const messageElement = document.createElement('div');

    // 情況一：使用者加入或離開的事件訊息
    if (message.type === 'JOIN' || message.type === 'LEAVE') {
        messageElement.className = 'text-muted text-center my-2 small';
        messageElement.textContent = message.type === 'JOIN' ? `${message.sender} 加入了聊天室` : `${message.sender} 離開了聊天室`;
    }
    // 情況二：一般的聊天訊息
    else {
        const isMyMessage = message.sender === currentUsername;
        const avatarUrl = `/images/memberAvatar/mem${message.memberId}.png`;
        const fallbackAvatarUrl = '/images/memberAvatar/defaultmem.png';

        // 根據是不是自己的訊息，決定頭像和文字的排列方向
        messageElement.className = `d-flex align-items-start gap-3 mb-3 ${isMyMessage ? 'flex-row-reverse' : ''}`;

        messageElement.innerHTML = `
            <div class="flex-shrink-0">
                <img src="${avatarUrl}" 
                     alt="${message.sender}" 
                     class="rounded-circle" 
                     style="width: 40px; height: 40px; object-fit: cover;"
                     onerror="this.onerror=null; this.src='${fallbackAvatarUrl}';">
            </div>
            <div class="flex-grow-1 ${isMyMessage ? 'text-end' : ''}">
                <div class="fw-bold small">${message.sender}</div>
                <div class="chat-bubble p-2 bg-light rounded d-inline-block mt-1">${message.content}</div>
            </div>
        `;
    }

    messagesArea.appendChild(messageElement);
    messagesArea.scrollTop = messagesArea.scrollHeight; // 自動滾動到底部
}


/**
 * 發送聊天訊息
 * @param {Event} event - 表單提交事件
 */
function sendMessage(event) {
    event.preventDefault();
    const messageContent = messageInput.value.trim();

    if (messageContent && stompClient) {
        const chatMessage = {
            sender: currentUsername,
            content: messageContent,
            type: 'CHAT',
            memberId: currentMemberId // 【新增】 保持 DTO 格式完整
        };

        stompClient.send(`/api/chat/${currentForumId}/sendMessage`, {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
}

/**
 * 斷開 WebSocket 連線 (切換討論區或離開頁面時呼叫)
 */
function disconnect() {
    if (stompClient !== null) {
        if (subscription) {
            subscription.unsubscribe();
            subscription = null;
        }
        stompClient.disconnect(() => {
            updateChatStatus('disconnected');
            console.log("WebSocket 已斷開");
        });
        stompClient = null;
    }
}

/**
 * 更新聊天室狀態顯示
 * @param {'connecting'|'connected'|'error'|'disconnected'} status
 */
function updateChatStatus(status) {
    if (!chatStatus) return;
    switch (status) {
        case 'connected':
            chatStatus.textContent = '已連線';
            chatStatus.className = 'badge bg-success';
            break;
        case 'error':
            chatStatus.textContent = '連線錯誤';
            chatStatus.className = 'badge bg-danger';
            break;
        case 'connecting':
            chatStatus.textContent = '連線中...';
            chatStatus.className = 'badge bg-warning text-dark';
            break;
        case 'disconnected':
        default:
            chatStatus.textContent = '未連線';
            chatStatus.className = 'badge bg-secondary';
            break;
    }
}

// 將主要函式掛載到 window 物件上，讓 allForum.js 可以呼叫
window.chatManager = {
    connect,
    disconnect
};