class PromptService {
    constructor() {
        this.baseURL = 'http://localhost:8080/api';
        this.accessToken = localStorage.getItem('accessToken');
    }

    /**
     * 새 프롬프트 생성
     */
    async createPrompt(title, content) {
        try {
            const response = await fetch(`${this.baseURL}/prompts`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.accessToken}`
                },
                body: JSON.stringify({
                    title: title,
                    content: content
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('프롬프트 생성 실패:', error);
            throw error;
        }
    }

    /**
     * 프롬프트 목록 조회
     */
    async getPrompts() {
        try {
            const response = await fetch(`${this.baseURL}/prompts`, {
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
            console.error('프롬프트 목록 조회 실패:', error);
            throw error;
        }
    }

    /**
     * 특정 프롬프트 조회
     */
    async getPrompt(promptId) {
        try {
            const response = await fetch(`${this.baseURL}/prompts/${promptId}`, {
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
            console.error('프롬프트 조회 실패:', error);
            throw error;
        }
    }

    /**
     * 프롬프트 수정
     */
    async updatePrompt(promptId, title, content) {
        try {
            const response = await fetch(`${this.baseURL}/prompts/${promptId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.accessToken}`
                },
                body: JSON.stringify({
                    title: title,
                    content: content
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('프롬프트 수정 실패:', error);
            throw error;
        }
    }

    /**
     * 프롬프트 삭제
     */
    async deletePrompt(promptId) {
        try {
            const response = await fetch(`${this.baseURL}/prompts/${promptId}`, {
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
            console.error('프롬프트 삭제 실패:', error);
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

export default PromptService; 