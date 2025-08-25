class ConversationService {
    constructor() {
        this.baseURL = 'http://localhost:8080/api';
        this.accessToken = localStorage.getItem('accessToken');
    }

    /**
     * 새 대화 생성
     */
    async createConversation(title = '새 대화') {
        try {
            const response = await fetch(`${this.baseURL}/conversations`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.accessToken}`
                },
                body: JSON.stringify({
                    title: title
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('대화 생성 실패:', error);
            throw error;
        }
    }

    /**
     * 대화 목록 조회
     */
    async getConversations(page = 0, size = 20) {
        try {
            const response = await fetch(`${this.baseURL}/conversations?page=${page}&size=${size}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${this.accessToken}`
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('대화 목록 조회 실패:', error);
            throw error;
        }
    }

    /**
     * 특정 대화 조회 (메시지 포함)
     */
    async getConversation(conversationId) {
        try {
            const response = await fetch(`${this.baseURL}/conversations/${conversationId}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${this.accessToken}`
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('대화 조회 실패:', error);
            throw error;
        }
    }

    /**
     * 대화 제목 업데이트
     */
    async updateConversation(conversationId, title) {
        try {
            const response = await fetch(`${this.baseURL}/conversations/${conversationId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.accessToken}`
                },
                body: JSON.stringify({
                    title: title
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('대화 업데이트 실패:', error);
            throw error;
        }
    }

    /**
     * 특정 대화의 메시지 조회
     */
    async getMessages(conversationId, page = 0, size = 50) {
        try {
            const response = await fetch(`${this.baseURL}/conversations/${conversationId}/messages?page=${page}&size=${size}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${this.accessToken}`
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('메시지 조회 실패:', error);
            throw error;
        }
    }

    /**
     * 대화 삭제
     */
    async deleteConversation(conversationId) {
        try {
            const response = await fetch(`${this.baseURL}/conversations/${conversationId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${this.accessToken}`
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return true;
        } catch (error) {
            console.error('대화 삭제 실패:', error);
            throw error;
        }
    }

    /**
     * 토큰 업데이트
     */
    setAccessToken(token) {
        this.accessToken = token;
        localStorage.setItem('accessToken', token);
    }
}

export default ConversationService; 