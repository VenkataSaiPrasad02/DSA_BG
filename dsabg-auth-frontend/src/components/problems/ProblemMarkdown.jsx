import ReactMarkdown from 'react-markdown';
import rehypeKatex from 'rehype-katex';
import remarkMath from 'remark-math';

const ProblemMarkdown = ({ content }) => {
    return (
        <div className="problem-markdown">
            <ReactMarkdown remarkPlugins={[remarkMath]} rehypePlugins={[rehypeKatex]}>
                {content || ''}
            </ReactMarkdown>
        </div>
    );
};

export default ProblemMarkdown;
