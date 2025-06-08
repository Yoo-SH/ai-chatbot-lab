import React, { useState, useEffect } from 'react';
import PromptService from '../services/PromptService';
import './PromptSelector.css';

// 기본 제공 프롬프트
const DEFAULT_PROMPTS = [
  {
    id: 'default-1',
    title: '역할 지정 프롬프트',
    content: '당신은 지금부터 [ ]의 역할을 해야 하고, 모든 대답을 마치 [ ]가 하는 것처럼 해야한다. 당신의 이름은 [ ]이다. 나는 [ ]에게 \'[ ]\'라는 질문을 할 것이다.',
    isDefault: true
  },
  {
    id: 'default-2',
    title: '후카츠 프롬프트',
    content: '#명령문\n당신은 ( )입니다. 이하의 제약조건과 입력문을 토대로 최고의 ( )을 출력해주세요.\n#제약조건\n- ( )\n- ( )\n#입력문\n( )\n#출력문',
    isDefault: true
  },
  {
    id: 'default-3',
    title: '예제 중심 프롬프트',
    content: '명령문:다음 예제와 같이 한국 속담을 영어로 번역해 주세요\n예시 1: "핑 대신 닭"\n응답: A chicken instead of a pheasant\n의미: Something is better than nothing',
    isDefault: true
  }
];

const PromptSelector = ({ onSelect, onClose }) => {
  const [userPrompts, setUserPrompts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showAddForm, setShowAddForm] = useState(false);
  const [editingPrompt, setEditingPrompt] = useState(null);
  const [formData, setFormData] = useState({ title: '', content: '' });
  
  const promptService = new PromptService();

  // 컴포넌트 마운트 시 사용자 프롬프트 로드
  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      promptService.setAccessToken(token);
      loadUserPrompts();
    } else {
      setLoading(false);
    }
  }, []);

  // 사용자 프롬프트 목록 로드
  const loadUserPrompts = async () => {
    try {
      setLoading(true);
      const prompts = await promptService.getPrompts();
      setUserPrompts(prompts);
      setError('');
    } catch (err) {
      console.error('프롬프트 로드 실패:', err);
      setError('프롬프트를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  // 프롬프트 선택
  const handleSelectPrompt = (prompt) => {
    onSelect(prompt.content);
  };

  // 새 프롬프트 추가 폼 표시
  const handleShowAddForm = () => {
    setFormData({ title: '', content: '' });
    setEditingPrompt(null);
    setShowAddForm(true);
  };

  // 프롬프트 편집 폼 표시
  const handleEditPrompt = (prompt, event) => {
    event.stopPropagation();
    setFormData({ title: prompt.title, content: prompt.content });
    setEditingPrompt(prompt);
    setShowAddForm(true);
  };

  // 프롬프트 저장 (생성 또는 수정)
  const handleSavePrompt = async () => {
    if (!formData.title.trim() || !formData.content.trim()) {
      setError('제목과 내용을 모두 입력해주세요.');
      return;
    }

    try {
      if (editingPrompt) {
        // 수정
        await promptService.updatePrompt(editingPrompt.id, formData.title.trim(), formData.content.trim());
      } else {
        // 생성
        await promptService.createPrompt(formData.title.trim(), formData.content.trim());
      }
      
      await loadUserPrompts();
      setShowAddForm(false);
      setEditingPrompt(null);
      setFormData({ title: '', content: '' });
      setError('');
    } catch (err) {
      console.error('프롬프트 저장 실패:', err);
      setError('프롬프트 저장에 실패했습니다.');
    }
  };

  // 프롬프트 삭제
  const handleDeletePrompt = async (promptId, event) => {
    event.stopPropagation();
    
    if (!window.confirm('이 프롬프트를 삭제하시겠습니까?')) {
      return;
    }

    try {
      await promptService.deletePrompt(promptId);
      await loadUserPrompts();
      setError('');
    } catch (err) {
      console.error('프롬프트 삭제 실패:', err);
      setError('프롬프트 삭제에 실패했습니다.');
    }
  };

  // 폼 취소
  const handleCancelForm = () => {
    setShowAddForm(false);
    setEditingPrompt(null);
    setFormData({ title: '', content: '' });
    setError('');
  };

  return (
    <div className="prompt-selector-modal">
      <div className="prompt-selector-content">
        <div className="prompt-selector-header">
          <h2>프롬프트 선택</h2>
          <button className="prompt-close-btn" onClick={onClose}>✕</button>
        </div>

        {error && (
          <div className="prompt-error-message">
            {error}
          </div>
        )}

        {!showAddForm ? (
          <div className="prompt-sections">
            {/* 기본 프롬프트 섹션 */}
            <div className="prompt-section">
              <h3>기본 프롬프트</h3>
              <ul className="prompt-list">
                {DEFAULT_PROMPTS.map((prompt) => (
                  <li key={prompt.id} className="prompt-item default-prompt" onClick={() => handleSelectPrompt(prompt)}>
                    <div className="prompt-item-header">
                      <strong>{prompt.title}</strong>
                    </div>
                    <pre className="prompt-content">{prompt.content}</pre>
                  </li>
                ))}
              </ul>
            </div>

            {/* 사용자 프롬프트 섹션 */}
            <div className="prompt-section">
              <div className="prompt-section-header">
                <h3>내 프롬프트</h3>
                <button className="prompt-add-btn" onClick={handleShowAddForm}>+ 추가</button>
              </div>
              
              {loading ? (
                <div className="prompt-loading">프롬프트를 불러오는 중...</div>
              ) : userPrompts.length === 0 ? (
                <div className="prompt-empty">생성된 프롬프트가 없습니다.</div>
              ) : (
                <ul className="prompt-list">
                  {userPrompts.map((prompt) => (
                    <li key={prompt.id} className="prompt-item user-prompt" onClick={() => handleSelectPrompt(prompt)}>
                      <div className="prompt-item-header">
                        <strong>{prompt.title}</strong>
                        <div className="prompt-item-actions">
                          <button 
                            className="prompt-edit-btn" 
                            onClick={(e) => handleEditPrompt(prompt, e)}
                            title="편집"
                          >
                            ✏️
                          </button>
                          <button 
                            className="prompt-delete-btn" 
                            onClick={(e) => handleDeletePrompt(prompt.id, e)}
                            title="삭제"
                          >
                            🗑️
                          </button>
                        </div>
                      </div>
                      <pre className="prompt-content">{prompt.content}</pre>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        ) : (
          /* 프롬프트 추가/편집 폼 */
          <div className="prompt-form">
            <h3>{editingPrompt ? '프롬프트 편집' : '새 프롬프트 추가'}</h3>
            <div className="prompt-form-field">
              <label>제목</label>
              <input
                type="text"
                value={formData.title}
                onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                placeholder="프롬프트 제목을 입력하세요"
                className="prompt-title-input"
              />
            </div>
            <div className="prompt-form-field">
              <label>내용</label>
              <textarea
                value={formData.content}
                onChange={(e) => setFormData({ ...formData, content: e.target.value })}
                placeholder="프롬프트 내용을 입력하세요"
                className="prompt-content-textarea"
                rows="8"
              />
            </div>
            <div className="prompt-form-actions">
              <button className="prompt-save-btn" onClick={handleSavePrompt}>
                {editingPrompt ? '수정' : '저장'}
              </button>
              <button className="prompt-cancel-btn" onClick={handleCancelForm}>
                취소
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default PromptSelector; 