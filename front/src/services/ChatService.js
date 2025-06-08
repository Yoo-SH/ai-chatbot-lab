class ChatService {
    constructor() {
        this.baseURL = 'http://localhost:8080/api';
        this.accessToken = localStorage.getItem('accessToken');
    }

    /**
     * 일반 HTTP 방식으로 메시지 전송
     */
    async sendMessage(conversationId, message, parentId = null) {
        try {
            const response = await fetch(`${this.baseURL}/chat`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.accessToken}`
                },
                body: JSON.stringify({
                    conversationId,
                    message,
                    parentId
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('메시지 전송 실패:', error);
            throw error;
        }
    }

    /**
     * SSE 스트리밍 방식으로 메시지 전송
     */
    async sendStreamingMessage(conversationId, message, parentId = null, onChunk = null, onComplete = null, onError = null) {
        try {
            // 1. 스트리밍 시작
            const startResponse = await fetch(`${this.baseURL}/chat/stream`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.accessToken}`
                },
                body: JSON.stringify({
                    conversationId,
                    message,
                    parentId
                })
            });

            if (!startResponse.ok) {
                throw new Error(`HTTP error! status: ${startResponse.status}`);
            }

            const { streamId } = await startResponse.json();

            // 2. SSE 연결 (토큰을 쿼리 파라미터로 전달)
            const sseUrl = `${this.baseURL}/chat/stream/${streamId}?token=${encodeURIComponent(this.accessToken)}`;
            const eventSource = new EventSource(sseUrl);

            let fullContent = '';
            let messageId = null;

            // 연결 성공 이벤트
            eventSource.addEventListener('connect', (event) => {
                try {
                    const data = JSON.parse(event.data);
                    console.log('SSE 연결 성공:', data);
                } catch (error) {
                    console.error('연결 이벤트 파싱 오류:', error);
                }
            });

            eventSource.addEventListener('chunk', (event) => {
                try {
                    const chunk = JSON.parse(event.data);
                    
                    switch (chunk.type) {
                        case 'START':
                            console.log('스트리밍 시작:', chunk);
                            break;
                            
                        case 'CONTENT':
                            fullContent = chunk.content;
                            if (onChunk) {
                                onChunk(chunk);
                            }
                            break;
                            
                        case 'END':
                            messageId = chunk.messageId;
                            fullContent = chunk.content;
                            eventSource.close();
                            
                            if (onComplete) {
                                onComplete({
                                    content: fullContent,
                                    messageId: messageId,
                                    streamId: streamId
                                });
                            }
                            break;
                            
                        case 'ERROR':
                            console.error('스트리밍 오류:', chunk.error);
                            eventSource.close();
                            if (onError) {
                                onError(new Error(chunk.error));
                            }
                            break;
                    }
                } catch (error) {
                    console.error('청크 파싱 오류:', error);
                    if (onError) {
                        onError(error);
                    }
                }
            });

            // 오류 이벤트 처리 (서버에서 명시적으로 보낸 오류)
            eventSource.addEventListener('error', (event) => {
                // 서버에서 보낸 오류 메시지가 있는 경우에만 처리
                if (event.data && event.data !== 'undefined') {
                    try {
                        const data = JSON.parse(event.data);
                        console.error('SSE 오류 이벤트:', data);
                        eventSource.close();
                        if (onError) {
                            onError(new Error(data.error || 'SSE 오류'));
                        }
                    } catch (error) {
                        console.error('오류 이벤트 파싱 실패:', error);
                        eventSource.close();
                        if (onError) {
                            onError(new Error('SSE 응답 파싱 오류'));
                        }
                    }
                }
            });

            // 연결 오류 처리 (네트워크 오류, 타임아웃 등)
            eventSource.onerror = (error) => {
                console.error('SSE 연결 오류:', error);
                
                // 연결 상태 확인
                if (eventSource.readyState === EventSource.CLOSED) {
                    console.log('SSE 연결이 정상적으로 종료되었습니다.');
                    return; // 정상 종료인 경우 오류로 처리하지 않음
                }
                
                if (eventSource.readyState === EventSource.CONNECTING) {
                    console.log('SSE 재연결 시도 중...');
                    return; // 재연결 시도 중인 경우 잠시 대기
                }
                
                // 실제 연결 오류인 경우에만 처리
                eventSource.close();
                if (onError) {
                    onError(new Error('서버와의 연결이 끊어졌습니다. 다시 시도해주세요.'));
                }
            };

            // 스트리밍 중단을 위한 함수 반환
            return {
                streamId,
                stop: () => {
                    eventSource.close();
                    this.stopStreaming(streamId);
                }
            };

        } catch (error) {
            console.error('스트리밍 메시지 전송 실패:', error);
            if (onError) {
                onError(error);
            }
            throw error;
        }
    }

    /**
     * 스트리밍 중단
     */
    async stopStreaming(streamId) {
        try {
            await fetch(`${this.baseURL}/chat/stream/${streamId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${this.accessToken}`
                }
            });
        } catch (error) {
            console.error('스트리밍 중단 실패:', error);
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

export default ChatService; 